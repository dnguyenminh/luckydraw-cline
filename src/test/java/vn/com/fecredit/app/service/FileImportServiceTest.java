package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import vn.com.fecredit.app.dto.participant.ImportParticipantResponse;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("File Import Service Tests")
class FileImportServiceTest {

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EventRepository eventRepository;

    private FileImportService fileImportService;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        fileImportService = new FileImportService(participantRepository, eventRepository);
        now = LocalDateTime.now();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(createTestEvent()));
    }

    @Test
    @DisplayName("Should successfully import valid CSV file")
    void shouldSuccessfullyImportValidCsvFile() {
        // Given
        String csvContent = "CustomerId,CardNumber,Email,FullName,PhoneNumber,Province\n" +
                          "CUST001,4111111111111111,test1@example.com,Test User 1,0987654321,Province 1\n" +
                          "CUST002,4111111111111112,test2@example.com,Test User 2,0987654322,Province 2";

        MockMultipartFile file = createMockCsvFile("participants.csv", csvContent);

        when(participantRepository.existsByCustomerId(anyString())).thenReturn(false);
        when(participantRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(participantRepository.existsByEmail(anyString())).thenReturn(false);
        when(participantRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // When
        ImportParticipantResponse response = fileImportService.importParticipants(file, 1L);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(2);
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getFailureCount()).isEqualTo(0);
        assertThat(response.getErrors()).isEmpty();
        assertThat(response.getSuccessMessages()).hasSize(2);
        verify(participantRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Should handle invalid CSV format")
    void shouldHandleInvalidCsvFormat() {
        // Given
        String csvContent = "Invalid,Format\nMissing,Required,Fields";
        MockMultipartFile file = createMockCsvFile("invalid.csv", csvContent);

        // When
        ImportParticipantResponse response = fileImportService.importParticipants(file, 1L);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(1);
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getFailureCount()).isEqualTo(1);
        assertThat(response.getErrors()).hasSize(1);
        assertThat(response.getErrors().get(0)).contains("Invalid row format");
        verify(participantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle duplicate entries")
    void shouldHandleDuplicateEntries() {
        // Given
        String csvContent = "CustomerId,CardNumber,Email,FullName,PhoneNumber,Province\n" +
                          "CUST001,4111111111111111,test1@example.com,Test User 1,0987654321,Province 1\n" +
                          "CUST001,4111111111111112,test2@example.com,Test User 2,0987654322,Province 2";

        MockMultipartFile file = createMockCsvFile("duplicates.csv", csvContent);
        when(participantRepository.existsByCustomerId("CUST001")).thenReturn(true);

        // When
        ImportParticipantResponse response = fileImportService.importParticipants(file, 1L);

        // Then
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getFailureCount()).isEqualTo(2);
        assertThat(response.getErrors()).hasSize(2);
        assertThat(response.getErrors().get(0)).contains("Customer ID already exists");
        verify(participantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle empty file")
    void shouldHandleEmptyFile() {
        // Given
        String csvContent = "CustomerId,CardNumber,Email,FullName,PhoneNumber,Province";
        MockMultipartFile file = createMockCsvFile("empty.csv", csvContent);

        // When
        ImportParticipantResponse response = fileImportService.importParticipants(file, 1L);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(0);
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getFailureCount()).isEqualTo(0);
        verify(participantRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate all unique fields")
    void shouldValidateAllUniqueFields() {
        // Given
        String csvContent = "CustomerId,CardNumber,Email,FullName,PhoneNumber,Province\n" +
                          "CUST001,4111111111111111,test@example.com,Test User,0987654321,Province";

        MockMultipartFile file = createMockCsvFile("test.csv", csvContent);

        when(participantRepository.existsByCustomerId("CUST001")).thenReturn(false);
        when(participantRepository.existsByCardNumber("4111111111111111")).thenReturn(true);
        // when(participantRepository.existsByEmail("test@example.com")).thenReturn(false);

        // When
        ImportParticipantResponse response = fileImportService.importParticipants(file, 1L);

        // Then
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getFailureCount()).isEqualTo(1);
        assertThat(response.getErrors().get(0)).contains("Card number already exists");
        verify(participantRepository, never()).save(any());
    }

    private Event createTestEvent() {
        return Event.builder()
                .id(1L)
                .name("Test Event")
                .code("TEST_EVENT")
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(7))
                .isActive(true)
                .build();
    }

    private MockMultipartFile createMockCsvFile(String filename, String content) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }
}
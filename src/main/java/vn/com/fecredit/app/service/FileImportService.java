package vn.com.fecredit.app.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.com.fecredit.app.dto.participant.ImportParticipantResponse;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileImportService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;

    @Transactional
    public ImportParticipantResponse importParticipants(MultipartFile file, Long eventId) {
        List<String> errors = new ArrayList<>();
        List<String> successMessages = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            // Skip header row
            rows.remove(0);

            for (String[] row : rows) {
                try {
                    if (row.length < 6) {
                        throw new IllegalArgumentException("Invalid row format");
                    }

                    String customerId = row[0].trim();
                    String cardNumber = row[1].trim();
                    String email = row[2].trim();
                    String fullName = row[3].trim();
                    String phoneNumber = row[4].trim();
                    String province = row[5].trim();

                    // Validate unique fields
                    if (participantRepository.existsByCustomerId(customerId)) {
                        throw new IllegalArgumentException("Customer ID already exists: " + customerId);
                    }
                    if (participantRepository.existsByCardNumber(cardNumber)) {
                        throw new IllegalArgumentException("Card number already exists: " + cardNumber);
                    }
                    if (participantRepository.existsByEmail(email)) {
                        throw new IllegalArgumentException("Email already exists: " + email);
                    }

                    Participant participant = Participant.builder()
                            .customerId(customerId)
                            .cardNumber(cardNumber)
                            .email(email)
                            .fullName(fullName)
                            .phoneNumber(phoneNumber)
                            .province(province)
                            .dailySpinLimit(3L) // Default value
                            .event(event)
                            .isActive(true)
                            .build();

                    participantRepository.save(participant);
                    successCount++;
                    successMessages.add("Successfully imported participant: " + fullName);
                } catch (Exception e) {
                    failureCount++;
                    errors.add("Row " + (successCount + failureCount) + ": " + e.getMessage());
                }
            }
        } catch (IOException | CsvException e) {
            throw new IllegalArgumentException("Error reading CSV file: " + e.getMessage());
        }

        return ImportParticipantResponse.builder()
                .totalProcessed(successCount + failureCount)
                .successCount(successCount)
                .failureCount(failureCount)
                .errors(errors)
                .successMessages(successMessages)
                .build();
    }
}
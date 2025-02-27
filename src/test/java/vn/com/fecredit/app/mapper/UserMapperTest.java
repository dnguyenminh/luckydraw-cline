package vn.com.fecredit.app.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.com.fecredit.app.dto.UserDTO;
import vn.com.fecredit.app.entity.User;
// import vn.com.fecredit.app.dto.UserInfoDto;
// import vn.com.fecredit.app.model.User;
import vn.com.fecredit.app.util.RoleTestData;
import vn.com.fecredit.app.util.UserTestData;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {
    private UserMapper mapper;
    private User testUser;

    @BeforeEach
    void setUp() {
        mapper = new UserMapper();
        testUser = UserTestData.createDefaultUser();
    }

    @Test
    void toDto_shouldMapAllFields() {
        // When
        UserDTO result = mapper.toDto(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(result.getFirstName()).isEqualTo(testUser.getFirstName());
        assertThat(result.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(result.getRoles()).isEqualTo(RoleTestData.createDefaultRoleNames());
        assertThat(result.getCreatedAt()).isEqualTo(testUser.getCreatedAt());
        assertThat(result.getUpdatedAt()).isEqualTo(testUser.getUpdatedAt());
    }

    @Test
    void toDto_withNullUser_shouldReturnNull() {
        // When
        UserDTO result = mapper.toDto(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void updateFromUserInfoDto_shouldUpdateOnlyNameFields() {
        // Given
        UserInfoDto updateDto = UserInfoDto.builder()
            .firstName("Updated")
            .lastName("Name")
            .build();
        String originalEmail = testUser.getEmail();

        // When
        mapper.updateFromUserInfoDto(updateDto, testUser);

        // Then
        assertThat(testUser.getFirstName()).isEqualTo(updateDto.getFirstName());
        assertThat(testUser.getLastName()).isEqualTo(updateDto.getLastName());
        assertThat(testUser.getEmail()).isEqualTo(originalEmail);
    }

    @Test
    void updateFromUserInfoDto_withNullValues_shouldNotUpdate() {
        // Given
        UserInfoDto updateDto = UserInfoDto.builder().build();
        String originalFirstName = testUser.getFirstName();
        String originalLastName = testUser.getLastName();
        String originalEmail = testUser.getEmail();

        // When
        mapper.updateFromUserInfoDto(updateDto, testUser);

        // Then
        assertThat(testUser.getFirstName()).isEqualTo(originalFirstName);
        assertThat(testUser.getLastName()).isEqualTo(originalLastName);
        assertThat(testUser.getEmail()).isEqualTo(originalEmail);
    }
}

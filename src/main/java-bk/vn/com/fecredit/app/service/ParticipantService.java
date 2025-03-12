package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.model.CreateParticipantRequest;
import vn.com.fecredit.app.model.UpdateParticipantRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ParticipantService {

    /**
     * Create new participant
     */
    ParticipantDTO createParticipant(CreateParticipantRequest request);

    /**
     * Update participant
     */
    ParticipantDTO updateParticipant(Long id, UpdateParticipantRequest request);

    /**
     * Get participant by ID
     */
    ParticipantDTO getParticipantById(Long id);

    /**
     * Get participant by user ID
     */
    ParticipantDTO getParticipantByUserId(Long userId);

    /**
     * Get participant by phone number
     */
    Optional<Participant> findByPhoneNumber(String phoneNumber);

    /**
     * Get participant by email
     */
    Optional<Participant> findByEmail(String email);

    /**
     * Get all participants
     */
    List<ParticipantDTO> getAllParticipants();

    /**
     * Get all active participants
     */
    List<ParticipantDTO> getActiveParticipants();

    /**
     * Get paginated participants
     */
    Page<ParticipantDTO> getParticipants(PageRequest pageRequest);

    /**
     * Search participants
     */
    Page<ParticipantDTO> searchParticipants(SearchRequest searchRequest);

    /**
     * Get participants by event ID
     */
    List<ParticipantDTO> getParticipantsByEventId(Long eventId);

    /**
     * Get participants by event location ID
     */
    List<ParticipantDTO> getParticipantsByLocationId(Long locationId);

    /**
     * Delete participant
     */
    void deleteParticipant(Long id);

    /**
     * Block participant
     */
    void blockParticipant(Long id, String reason);

    /**
     * Unblock participant
     */
    void unblockParticipant(Long id);

    /**
     * Check if participant is blocked
     */
    boolean isBlocked(Long id);

    /**
     * Update participant status
     */
    void updateParticipantStatus(Long id, boolean active);

    /**
     * Get participant spin count
     */
    int getSpinCount(Long id);

    /**
     * Update participant spin count
     */
    void updateSpinCount(Long id, int spinCount);

    /**
     * Reset participant spin count
     */
    void resetSpinCount(Long id);

    /**
     * Add spin count to participant
     */
    void addSpinCount(Long id, int additionalSpins);

    /**
     * Get participant last spin time
     */
    LocalDateTime getLastSpinTime(Long id);

    /**
     * Update participant last spin time
     */
    void updateLastSpinTime(Long id, LocalDateTime lastSpinTime);

    /**
     * Check if participant can spin
     */
    boolean canSpin(Long id);

    /**
     * Get participant's total rewards
     */
    int getTotalRewards(Long id);

    /**
     * Get participant's total points
     */
    int getTotalPoints(Long id);

    /**
     * Add points to participant
     */
    void addPoints(Long id, int points);

    /**
     * Deduct points from participant
     */
    void deductPoints(Long id, int points);

    /**
     * Check if phone number exists
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Count participants
     */
    long countParticipants();

    /**
     * Count active participants
     */
    long countActiveParticipants();

    /**
     * Count participants by event
     */
    long countParticipantsByEvent(Long eventId);

    /**
     * Count participants by location
     */
    long countParticipantsByLocation(Long locationId);
}

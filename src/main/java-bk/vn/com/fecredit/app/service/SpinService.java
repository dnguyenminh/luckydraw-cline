package vn.com.fecredit.app.service;

import vn.com.fecredit.app.dto.SpinRequest;
import vn.com.fecredit.app.dto.SpinResultDTO;
import vn.com.fecredit.app.exception.SpinNotAllowedException;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.service.common.BaseService;

import java.time.LocalDateTime;

/**
 * Service for handling spinning operations in the lucky draw system
 */
public interface SpinService extends BaseService {

    /**
     * Process a spin request and return the result
     * @param request The spin request containing event and participant details
     * @return The result of the spin including any rewards won
     * @throws SpinNotAllowedException if the spin is not allowed
     * @throws ResourceNotFoundException if required resources are not found
     */
    SpinResultDTO spin(SpinRequest request);

    /**
     * Process a spin request and get the result synchronously
     * @param request The spin request
     * @return The result of the spin
     */
    SpinResultDTO spinAndGetResult(SpinRequest request);

    /**
     * Process a spin by participant ID
     * @param participantId The ID of the participant
     * @return The result of the spin
     */
    SpinResultDTO processSpin(long participantId);

    /**
     * Get the latest spin result for a participant
     * @param participantId The ID of the participant
     * @return The latest spin result
     */
    SpinResultDTO getLatestSpinResult(long participantId);

    /**
     * Check if a spin is allowed for the given request
     * @param request The spin request to validate
     * @return true if spinning is allowed, false otherwise
     */
    boolean isSpinAllowed(SpinRequest request);

    /**
     * Get the number of remaining spins for a participant in an event
     * @param eventId The ID of the event
     * @param participantId The ID of the participant
     * @return Number of spins remaining
     */
    Long getRemainingSpins(Long eventId, Long participantId);

    /**
     * Check if a golden hour is currently active
     * @param eventId The ID of the event
     * @param eventLocationId The ID of the event location
     * @param timestamp The time to check
     * @return true if golden hour is active, false otherwise
     */
    boolean isGoldenHourActive(Long eventId, Long eventLocationId, LocalDateTime timestamp);

    /**
     * Get the current golden hour multiplier if active
     * @param eventId The ID of the event
     * @param eventLocationId The ID of the event location
     * @return The multiplier value, defaults to 1.0 if no golden hour is active
     */
    double getGoldenHourMultiplier(Long eventId, Long eventLocationId);

    /**
     * Get spin history for a participant in an event
     * @param eventId The ID of the event
     * @param participantId The ID of the participant 
     * @return Summary of participant's spin history
     */
    SpinResultDTO.SpinHistoryStats getSpinHistory(Long eventId, Long participantId);

    /**
     * Initialize spin settings for a participant in an event
     * @param eventId The ID of the event
     * @param participantId The ID of the participant
     * @param initialSpins Number of spins to allocate
     */
    void initializeSpins(Long eventId, Long participantId, Long initialSpins);

    /**
     * Reset daily spin count for a participant
     * @param eventId The ID of the event
     * @param participantId The ID of the participant
     */
    void resetDailySpins(Long eventId, Long participantId);
}

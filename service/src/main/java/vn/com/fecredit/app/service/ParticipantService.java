package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.dto.ParticipantDTO.CreateRequest;
import vn.com.fecredit.app.dto.ParticipantDTO.UpdateRequest;
import vn.com.fecredit.app.dto.ParticipantDTO.Response;

public interface ParticipantService {
    
    Response createParticipant(CreateRequest request);
    
    Response updateParticipant(Long id, UpdateRequest request);
    
    Response getById(Long id);
    
    void deleteParticipant(Long id);
    
    Page<Response> searchParticipants(String searchText, Long eventId, Integer status, Pageable pageable);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    boolean existsByPhoneNumberAndEventId(String phoneNumber, Long eventId);
    
    boolean existsByIdentityNumber(String identityNumber);
    
    boolean existsByIdentityNumberAndEventId(String identityNumber, Long eventId);
    
    Response findParticipant(String phoneNumber, Long eventId);
    
    Response findParticipantByIdentity(String identityNumber, Long eventId);
    
    void decrementRemainingSpins(Long participantId);
}

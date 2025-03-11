package vn.com.fecredit.app.service;

import vn.com.fecredit.app.dto.GoldenHourDTO;
import java.util.List;

public interface GoldenHourService {
    GoldenHourDTO.Response create(GoldenHourDTO.CreateRequest request);
    GoldenHourDTO.Response update(Long id, GoldenHourDTO.UpdateRequest request);
    void delete(Long id);
    GoldenHourDTO.Response getById(Long id);
    List<GoldenHourDTO.Response> getAllByEventId(Long eventId);
    GoldenHourDTO.Response getCurrentGoldenHour(Long eventId);
}

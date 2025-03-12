package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.dto.projection.HourlyStatsProjection;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.EventLocationMapper;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;
import vn.com.fecredit.app.service.EventLocationService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventLocationServiceImpl implements EventLocationService {

    private final EventLocationRepository locationRepository;
    private final SpinHistoryRepository spinHistoryRepository;
    private final EventLocationMapper locationMapper;

    @Override
    public EventLocationDTO.Response createLocation(EventLocationDTO.CreateRequest request) {
        EventLocation location = locationMapper.toEntity(request);
        location = locationRepository.save(location);
        return locationMapper.toResponse(location);
    }

    @Override
    public EventLocationDTO.Response updateLocation(Long id, EventLocationDTO.UpdateRequest request) {
        EventLocation location = getLocationOrThrow(id);
        locationMapper.updateEntity(location, request);
        location = locationRepository.save(location);
        return locationMapper.toResponse(location);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EventLocationDTO.Response> getLocation(Long id) {
        return locationRepository.findById(id)
                .map(locationMapper::toResponse);
    }

    @Override
    public void deleteLocation(Long id) {
        EventLocation location = getLocationOrThrow(id);
        location.setStatus(EntityStatus.DELETED);
        locationRepository.save(location);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventLocationDTO.Response> listLocations(Pageable pageable) {
        return locationRepository.findAll(pageable)
                .map(locationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventLocationDTO.Response> listActiveLocations(Pageable pageable) {
        return locationRepository.findAllByStatus(EntityStatus.ACTIVE, pageable)
                .map(locationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocationDTO.Response> listEventLocations(Long eventId) {
        return locationRepository.findAllByEventId(eventId).stream()
                .map(locationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void activateLocation(Long id) {
        EventLocation location = getLocationOrThrow(id);
        location.setStatus(EntityStatus.ACTIVE);
        locationRepository.save(location);
    }

    @Override
    public void deactivateLocation(Long id) {
        EventLocation location = getLocationOrThrow(id);
        location.setStatus(EntityStatus.INACTIVE);
        locationRepository.save(location);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocationDTO.Summary> getLocationSummaries() {
        return locationRepository.findAll().stream()
                .map(locationMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocationDTO.Summary> getActiveLocationSummaries() {
        return locationRepository.findAllByStatus(EntityStatus.ACTIVE).stream()
                .map(locationMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventLocationDTO.Summary> getEventLocationSummaries(Long eventId) {
        return locationRepository.findAllByEventId(eventId).stream()
                .map(locationMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventLocationDTO.Statistics getLocationStatistics(Long id, LocalDateTime startDate, LocalDateTime endDate) {
        EventLocation location = getLocationOrThrow(id);
        long totalSpins = spinHistoryRepository.countByLocationAndSpinTimeBetween(location, startDate, endDate);
        long winningSpins = spinHistoryRepository.countByLocationAndWonTrueAndSpinTimeBetween(location, startDate, endDate);

        Map<String, Integer> spinsByLocation = new HashMap<>();
        spinsByLocation.put(location.getName(), (int) totalSpins);

        Map<String, Double> winRatesByLocation = new HashMap<>();
        winRatesByLocation.put(location.getName(), calculateWinRate(winningSpins, totalSpins));

        return EventLocationDTO.Statistics.builder()
                .totalLocations(1L)
                .activeLocations(location.getStatus() == EntityStatus.ACTIVE ? 1L : 0L)
                .spinsByLocation(spinsByLocation)
                .winRatesByLocation(winRatesByLocation)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EventLocationDTO.LocationStatistics getEventLocationStatistics(Long id) {
        EventLocation location = getLocationOrThrow(id);
        List<HourlyStatsProjection> hourlyStats = spinHistoryRepository.findHourlyStatsByLocation(location);

        return EventLocationDTO.LocationStatistics.builder()
                .id(location.getId())
                .locationName(location.getName())
                .totalSpins(spinHistoryRepository.countByLocation(location))
                .winningSpins(spinHistoryRepository.countByLocationAndWonTrue(location))
                .winRate(calculateOverallWinRate(location))
                .hourlyStats(new HashSet<>(hourlyStats.stream()
                    .map(stats -> EventLocationDTO.HourlyStats.builder()
                        .hour(stats.getHour())
                        .totalSpins(stats.getTotalSpins())
                        .winningSpins(stats.getWinningSpins())
                        .winRate(stats.getWinRate())
                        .build())
                    .collect(Collectors.toList())))
                .build();
    }

    @Override
    public void updateSpinLimits(Long id, Integer newDailyLimit) {
        EventLocation location = getLocationOrThrow(id);
        location.setDailySpinLimit(newDailyLimit);
        locationRepository.save(location);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getRemainingSpinsToday(Long id) {
        EventLocation location = getLocationOrThrow(id);
        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
        
        long usedSpins = spinHistoryRepository.countByLocationAndSpinTimeBetween(
            location, startOfDay, endOfDay);
        
        return Math.max(0L, location.getDailySpinLimit() - usedSpins);
    }

    @Override
    @Transactional
    public Long decrementRemainingSpins(Long id) {
        Long remaining = getRemainingSpinsToday(id);
        if (remaining <= 0) {
            throw new IllegalStateException("No remaining spins for today");
        }
        return remaining - 1;
    }

    private EventLocation getLocationOrThrow(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    }

    private double calculateWinRate(long wins, long total) {
        return total == 0 ? 0.0 : (double) wins / total;
    }

    private double calculateOverallWinRate(EventLocation location) {
        long total = spinHistoryRepository.countByLocation(location);
        long wins = spinHistoryRepository.countByLocationAndWonTrue(location);
        return calculateWinRate(wins, total);
    }
}

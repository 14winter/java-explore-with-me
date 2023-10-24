package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.exception.WrongTimeException;
import ru.practicum.ewm.stats.mapper.EndpointHitMapper;
import ru.practicum.ewm.stats.mapper.ViewStatsMapper;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.model.ViewStats;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public void save(EndpointHitDto endpointHitDto) {
        EndpointHit createdEndpointHit = statsRepository.save(EndpointHitMapper.toEndpointHit(endpointHitDto));
        log.info("Добавлена статистика c id{}", createdEndpointHit.getId());
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new WrongTimeException("Конец даты не может быть раньше начала.");
        }
        List<ViewStats> allViewStats;
        if (unique) {
            allViewStats = statsRepository.getByUniqueIp(start, end, uris);
        } else {
            allViewStats = statsRepository.getEndpointHit(start, end, uris);
        }
        List<ViewStatsDto> result = allViewStats.stream().map(ViewStatsMapper::toViewStatsDto).collect(Collectors.toList());
        log.info("Получено статистики: {}", result.size());
        return result;
    }
}

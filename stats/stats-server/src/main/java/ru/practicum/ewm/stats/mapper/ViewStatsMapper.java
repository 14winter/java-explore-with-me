package ru.practicum.ewm.stats.mapper;

import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.model.ViewStats;

public class ViewStatsMapper {
    public static ViewStats toViewStats(ViewStatsDto viewStatsDto) {
        return ViewStats.builder()
                .app(viewStatsDto.getApp())
                .uri(viewStatsDto.getUri())
                .hits(viewStatsDto.getHits()).build();
    }

    public static ViewStatsDto toViewStatsDto(ViewStats viewStats) {
        return ViewStatsDto.builder()
                .app(viewStats.getApp())
                .uri(viewStats.getUri())
                .hits(viewStats.getHits()).build();
    }
}

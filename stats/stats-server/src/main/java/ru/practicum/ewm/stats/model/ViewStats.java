package ru.practicum.ewm.stats.model;

import lombok.*;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStats {
    private String app;

    private String uri;

    private Long hits;
}

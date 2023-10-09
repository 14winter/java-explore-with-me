package ru.practicum.ewm.stats.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stats")
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "app")
    private String app;

    @Column(nullable = false, name = "uri")
    private String uri;

    @Column(nullable = false, name = "ip")
    private String ip;

    @Column(name = "created")
    private LocalDateTime timestamp;
}

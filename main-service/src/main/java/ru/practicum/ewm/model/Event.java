package ru.practicum.ewm.model;

import lombok.*;
import ru.practicum.ewm.enums.EventState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "annotation")
    private String annotation;
    @JoinColumn(name = "cat_id")
    @ManyToOne
    private Category category;
    @Transient
    @Column(name = "confirmed_requests")
    private Long confirmedRequests;
    @Column(name = "created_on")
    private LocalDateTime createdOn;
    @Column(name = "description")
    private String description;
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    @JoinColumn(name = "initiator_id")
    @ManyToOne
    private User initiator;
    @JoinColumn(name = "loc_id")
    @ManyToOne
    private Location location;
    @Column(name = "paid")
    private Boolean paid;
    @Column(name = "participant_limit")
    private Long participantLimit;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(name = "request_moderation")
    private Boolean requestModeration;
    @Enumerated(EnumType.STRING)
    @Column(name = "state_event")
    private EventState state;
    @Column(name = "title")
    private String title;
    @Transient
    @Column(name = "views")
    private Long views;
}

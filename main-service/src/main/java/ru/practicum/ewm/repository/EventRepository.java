package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.enums.EventState;
import ru.practicum.ewm.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Boolean existsByCategoryId(Long categoryId);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByInitiatorIdAndId(Long userId, Long eventId);

    @Query("SELECT e FROM Event AS e " +
            "WHERE (:users IS NULL OR e.initiator.id IN :users) " +
            "AND (:states IS NULL OR e.state IN :states) " +
            "AND (:categories IS NULL OR e.category.id IN :categories) " +
            "AND (CAST(:start AS date) IS NULL OR e.eventDate >= :start) " +
            "AND (CAST(:end AS date) IS NULL OR e.eventDate <= :end) ")
    List<Event> findByFiltersAdmin(List<Long> users, List<EventState> states, List<Long> categories,
                                   LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT e FROM Event AS e " +
            "WHERE (:text IS NULL " +
            "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(e.annotation) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND (:state IS NULL OR e.state = :state) " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:categories IS NULL OR e.category.id IN (:categories)) " +
            "AND (CAST(:start AS date) IS NULL OR e.eventDate >= :start) " +
            "AND (CAST(:end AS date) IS NULL OR e.eventDate <= :end) ")
    List<Event> findByFiltersPublic(String text, List<Long> categories, Boolean paid, LocalDateTime start,
                                    LocalDateTime end, EventState state, Pageable pageable);
}

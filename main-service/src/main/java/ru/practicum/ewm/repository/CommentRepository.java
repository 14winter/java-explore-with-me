package ru.practicum.ewm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByAuthorId(Long userId, Pageable pageable);

    List<Comment> findByEventId(Long eventId, Pageable pageable);

    @Query("SELECT c FROM Comment AS c " +
            "WHERE (:text IS NULL OR LOWER(c.text) LIKE LOWER(CONCAT('%', :text, '%')))" +
            "AND (:users IS NULL OR c.author.id IN :users)" +
            "AND (:events IS NULL OR c.event.id IN :events)" +
            "AND (CAST(:start AS date) IS NULL OR c.created >= :start)" +
            "AND (CAST(:end AS date) IS NULL OR c.created <= :end) ")
    List<Comment> findByFilters(String text, List<Long> users, List<Long> events, LocalDateTime start, LocalDateTime end, Pageable pageable);
}

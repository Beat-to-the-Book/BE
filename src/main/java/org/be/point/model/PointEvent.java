package org.be.point.model;

import jakarta.persistence.*;
import lombok.*;
import org.be.auth.model.User;
import java.time.LocalDateTime;

@Entity
@Table(name = "point_event")
@Getter @NoArgsConstructor @AllArgsConstructor
public class PointEvent {

    public enum Reason {
        THROW_BOOK_SUCCESS,
        MILESTONE_10, MILESTONE_20, MILESTONE_30, MILESTONE_40, MILESTONE_50
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int delta;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private Reason reason;

    private LocalDateTime createdAt = LocalDateTime.now();

    public PointEvent(User user, int delta, Reason reason) {
        this.user = user;
        this.delta = delta;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }
}
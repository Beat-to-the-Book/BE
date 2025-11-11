package org.be.bookshelf.model;

import jakarta.persistence.*;
import lombok.*;
import org.be.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookshelf")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Bookshelf {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Lob
    @Column(name = "decorations_json", columnDefinition = "LONGTEXT", nullable = false)
    private String decorationsJson;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
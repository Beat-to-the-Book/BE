package org.be.book.model;

import jakarta.persistence.*;
import lombok.*;
import org.be.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User author;

    @ManyToOne(optional = false)
    private Book book;

    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    private int rating;

    @Column(name = "is_public", nullable = false)
    private Boolean publicVisible; // ✅ 필드명 변경 (DB 컬럼명은 그대로)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
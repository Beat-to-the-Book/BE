package org.be.book.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.be.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Book book;

    @ManyToOne(optional = false)
    private User author;

    @Min(0) @Max(5)
    private int rating;

    private String comment;

    private LocalDateTime createdAt;

    public void update(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }
}
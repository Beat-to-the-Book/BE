package org.be.behavior.model;

import jakarta.persistence.*;
import lombok.*;
import org.be.auth.model.User;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "user_behavior")
public class UserBehavior {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private Long bookId;
    private int stayTime;
    private int scrollDepth;
}

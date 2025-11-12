package org.be.community.entity;

import jakarta.persistence.*;
import lombok.*;
import org.be.auth.model.User;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "root_id")
    private Comment root;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private byte depth = 0;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private boolean edited = false;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static Comment newRoot(CommunityPost post, User author, String content) {
        Comment c = Comment.builder()
                .post(post).author(author).content(content)
                .depth((byte)0)
                .build();
        return c;
    }

    public static Comment newReply(CommunityPost post, User author, Comment parent, String content) {
        Comment c = Comment.builder()
                .post(post).author(author).content(content)
                .parent(parent)
                .root(parent.getRoot() == null ? parent : parent.getRoot())
                .depth((byte)(parent.getDepth() + 1))
                .build();
        return c;
    }
}

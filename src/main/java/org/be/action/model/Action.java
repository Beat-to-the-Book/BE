package org.be.action.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.be.auth.model.User;
import org.be.book.model.Book;

@Entity
@Table(name = "actions")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "bookId", nullable = false)
    private Book book;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType actionType; // click, search, purchase, cart, like, view_time

    @NotNull
    @Column(nullable = false)
    private float actionValue;

    public enum ActionType {
        CLICK, SEARCH, PURCHASE, CART, LIKE, VIEW_TIME
    }

    public Action() {}

    public Action(User user, Book book, ActionType actionType, float actionValue) {
        this.user = user;
        this.book = book;
        this.actionType = actionType;
        this.actionValue = actionValue;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public ActionType getActionType() { return actionType; }
    public void setActionType(ActionType actionType) { this.actionType = actionType; }

    public float getActionValue() { return actionValue; }
    public void setActionValue(float actionValue) { this.actionValue = actionValue; }
}

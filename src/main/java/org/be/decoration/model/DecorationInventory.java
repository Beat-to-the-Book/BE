// org/be/decoration/model/DecorationInventory.java
package org.be.decoration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.be.auth.model.User;

@Entity
@Table(
        name = "decoration_inventory",
        uniqueConstraints = @UniqueConstraint(name="uk_decoration_inventory_user_type", columnNames = {"user_id","type"})
)
@Getter
@NoArgsConstructor
public class DecorationInventory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name="type", length=20, nullable=false)
    private DecorationType type;

    @Column(name="count", nullable=false)
    private int count = 0;

    public DecorationInventory(User user, DecorationType type) {
        this.user = user;
        this.type = type;
        this.count = 0;
    }

    public void increase(int delta) {
        this.count += delta;
    }
}
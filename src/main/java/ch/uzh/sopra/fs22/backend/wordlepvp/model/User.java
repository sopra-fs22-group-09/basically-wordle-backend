package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")  // because Postgres is stupid we should not use user without quotes, but we use plural anyways
public class User extends BaseEntity implements Serializable {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true) // ...
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column
    private String avatarID;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "friends",
            joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)},
            inverseJoinColumns = {@JoinColumn(name = "friend_id", referencedColumnName = "id", nullable = false)})
    @ToString.Exclude
    private Set<User> friends;

    @OneToMany(fetch = FetchType.EAGER)
    @ToString.Exclude
    private Set<Score> scores;

    //TODO: no kei klass ..
    //private Achievement[] achievements;

    //    @Column(nullable = false)
    @Column
    private boolean tutorialCompleted;

    //TODO EIG JSON ..
//    @Column(nullable = false)
    @Column
    private String settings;

    //    @Column(nullable = false)
    @Column
    private UserStatus status;

    //    @Column(nullable = false)
    @Column(nullable = false)
    private boolean activated;

    //    @Column(nullable = false)
    @Column
    private String resetToken;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

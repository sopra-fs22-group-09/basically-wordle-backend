package ch.uzh.sopra.fs22.backend.wordlepvp.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")  // because Postgres is stupid we should not use user without quotes, but we use plural anyways
public class User implements Serializable {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "uuid2")
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true) // ...
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

//    @Column(name = "passwordHash", )
    private String passwordHash;
}

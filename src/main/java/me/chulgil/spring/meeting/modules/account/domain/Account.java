package me.chulgil.spring.meeting.modules.account.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private String nickname;

    private String password;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joindAt;

    private String bio;

    private String url;

    private String job;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String img;

    private boolean createdByEmail;

    private boolean createdByWeb;
    private boolean updatedByWeb;

    private boolean enrollmentByEmail;

    private boolean enrollmentByWeb;

    private boolean notifyByEmail;

    private boolean notifyByWeb;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }

    public void completeSignUp() {
        this.emailVerified = true;
        this.joindAt =LocalDateTime.now();
    }
}

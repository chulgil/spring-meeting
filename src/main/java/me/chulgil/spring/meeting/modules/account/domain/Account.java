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

    private String occupation;

    private boolean emailVerified;

    private String emailCheckToken;

    private LocalDateTime joinedAt;
    private LocalDateTime emailCheckedAt;

    private String bio;

    private String url;

    private String job;

    private String location;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String profileImage;

    private boolean createdByEmail;

    private boolean createdByWeb;
    private boolean updatedByWeb;

    private boolean enrollmentByEmail;

    private boolean enrollmentByWeb;

    private boolean notifyByEmail;

    private boolean notifyByWeb;

    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
        this.emailCheckedAt = LocalDateTime.now();
    }

    public void  completeSignUp() {
        this.emailVerified = true;
        this.joinedAt =LocalDateTime.now();
    }

    public boolean isValidToken(String token) {
        return this.emailCheckToken.equals(token);
    }

    public boolean canSendConfirmEmail() {
        return this.emailCheckedAt.isBefore(LocalDateTime.now().minusMinutes(1));
    }
}

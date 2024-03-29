package me.chulgil.spring.meeting.modules.account.domain;

import lombok.*;
import me.chulgil.spring.meeting.modules.tag.Tag;
import me.chulgil.spring.meeting.modules.zone.Zone;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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

    private boolean createdByWeb = true;
    private boolean updatedByWeb = true;

    private boolean enrollmentByEmail = false;

    private boolean enrollmentByWeb = true;

    private boolean notifyByEmail = false;

    private boolean notifyByWeb = true;

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

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

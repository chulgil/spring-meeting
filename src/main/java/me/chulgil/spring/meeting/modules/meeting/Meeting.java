package me.chulgil.spring.meeting.modules.meeting;

import lombok.*;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.domain.UserAccount;
import me.chulgil.spring.meeting.modules.tag.Tag;
import me.chulgil.spring.meeting.modules.zone.Zone;

import javax.persistence.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@NamedEntityGraph(name = "Meeting.withAllRelations", attributeNodes = {
        @NamedAttributeNode("tags"),
        @NamedAttributeNode("zones"),
        @NamedAttributeNode("managers"),
        @NamedAttributeNode("members")})

@Entity
@Getter @Setter
@EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Meeting {

    @Id @GeneratedValue
    private Long id;

    @ManyToMany
    private Set<Account> managers = new HashSet<>();

    @ManyToMany
    private Set<Account> members = new HashSet<>();

    @Column(unique = true)
    private String path;

    private String title;

    private String shortDescription;

    @Lob @Basic(fetch = FetchType.EAGER)
    private String fullDescription;


    @Lob @Basic(fetch = FetchType.EAGER)
    private String image;

    @ManyToMany
    private Set<Zone> zones = new HashSet<>();

    @ManyToMany
    private Set<Tag> tags = new HashSet<>();

    private LocalDateTime publishedDateTime;

    private LocalDateTime closedDateTime;

    private LocalDateTime recruitingDateTime;

    private boolean recruiting;

    private boolean published;

    private boolean closed;

    private boolean useBanner;

    private int memberCount;

    public void addManager(Account account) {
        this.managers.add(account);
    }

    public boolean isJoinable(UserAccount userAccount) {
        Account account = userAccount.getAccount();
        return this.isPublished()
                && this.isRecruiting()
                && !this.members.contains(account)
                && !this.managers.contains(account);
    }

    public boolean isMember(UserAccount userAccount) {
        return this.members.contains(userAccount.getAccount());
    }

    public boolean isManager(UserAccount userAccount) {
        return this.managers.contains(userAccount.getAccount());
    }

    public void addMember(Account account) {
        this.getMembers().add(account);
        this.memberCount++;
    }

    public void removeMember(Account account) {
        this.getMembers().remove(account);
        this.memberCount--;
    }

    public boolean isManagedBy(Account account) {
        return this.getManagers().contains(account);
    }

    public String getEncodedPath() {
        return URLEncoder.encode(this.path, StandardCharsets.UTF_8);
    }

    public String getEncodedURL() {
        return "/" + this.getEncodedPath();
    }

    public void publish(boolean publish) {

        if (this.closed || this.published) {
            throw new RuntimeException("처리할 수 없습니다. 모임을 이미 활성화했거나 종료 했습니다.");
        }

        if (publish) {
            this.published = true;
        } else {
            this.setClosed(true);
        }

        this.publishedDateTime = LocalDateTime.now();
    }

    public boolean canUpdateRecruiting() {
        return this.published && this.recruitingDateTime == null
                || this.recruitingDateTime.isBefore(LocalDateTime.now().minusHours(1));
    }
    public void setRecruiting(boolean enable) {
        if (canUpdateRecruiting()) {
            this.recruiting = enable;
            this.recruitingDateTime = LocalDateTime.now();
        } else {
            throw new RuntimeException("처리할 수 없습니다. 이미 활성화된 인원모집은 한시간 뒤 수정할 수 있습니다.");
        }
    }

    public boolean isRemovable() {
        return !this.published; // 이미 활성화 했던 모임은 삭제할 수 없다.
    }
}

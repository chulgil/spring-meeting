package me.chulgil.spring.meeting.modules.meeting;

import jdk.jfr.Name;
import lombok.*;
import me.chulgil.spring.meeting.modules.account.domain.Account;
import me.chulgil.spring.meeting.modules.account.domain.UserAccount;
import me.chulgil.spring.meeting.modules.tag.Tag;
import me.chulgil.spring.meeting.modules.zone.Zone;
import org.springframework.security.core.userdetails.User;

import javax.persistence.*;
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
        return this.isPublished() && this.isRecruiting()
                && !this.members.contains(account) && !this.managers.contains(account);
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
}

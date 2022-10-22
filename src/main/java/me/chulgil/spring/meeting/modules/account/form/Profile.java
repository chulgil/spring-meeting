package me.chulgil.spring.meeting.modules.account.form;


import lombok.Data;
import lombok.NoArgsConstructor;
import me.chulgil.spring.meeting.modules.account.domain.Account;

@Data
@NoArgsConstructor
public class Profile {

    private String bio;

    private String url;

    private String occupation;

    private String location;
    private String profileImage;

    public Profile(Account account) {
        this.bio = account.getBio();
        this.url = account.getUrl();
        this.occupation = account.getOccupation();
        this.location = account.getLocation();
        this.profileImage = account.getProfileImage();
    }
}

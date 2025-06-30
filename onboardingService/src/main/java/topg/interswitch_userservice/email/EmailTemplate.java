package topg.interswitch_userservice.email;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum  EmailTemplate {
    ACTIVATE_ACCOUNT("activate_account");

    private String name;


}

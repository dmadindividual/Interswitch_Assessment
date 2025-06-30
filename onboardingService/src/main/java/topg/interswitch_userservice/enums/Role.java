package topg.interswitch_userservice.enums;

import lombok.Getter;

@Getter
public enum Role {
    CUSTOMER("A User");

    private final String description;

    Role(String description) {
        this.description = description;
    }

}

package topg.interswitch_userservice.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidJwtToken extends  AppBaseException {
    public InvalidJwtToken(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}

package topg.interswitch_userservice.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidLoginDetailsException extends AppBaseException {
    public InvalidLoginDetailsException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}

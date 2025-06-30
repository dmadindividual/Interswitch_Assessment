package topg.interswitch_userservice.exceptions;

import org.springframework.http.HttpStatus;

public class TokenNotFoundException extends AppBaseException{
    public TokenNotFoundException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}

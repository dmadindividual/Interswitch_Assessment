package topg.interswitch_userservice.user.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import topg.interswitch_userservice.user.dto.AuthenticationRequest;
import topg.interswitch_userservice.user.dto.AuthenticationResponse;
import topg.interswitch_userservice.user.dto.UserRequest;
import topg.interswitch_userservice.user.dto.UserResponseDto;
import topg.interswitch_userservice.user.service.UserServiceImpl;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserServiceImpl userService;


    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequest userRequest) throws MessagingException {
        UserResponseDto response = userService.createUser(userRequest);
        return ResponseEntity.ok(response);
    }



    @PostMapping("/login")
    public  ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest loginRequestDto){
        AuthenticationResponse data = userService.authenticate(loginRequestDto);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/activate/{token}")

    public ResponseEntity<String> activateAccount(@PathVariable("token") String token) throws MessagingException {

        String result = userService.activateAccountByToken(token);
        return ResponseEntity.ok(result);



    }

}

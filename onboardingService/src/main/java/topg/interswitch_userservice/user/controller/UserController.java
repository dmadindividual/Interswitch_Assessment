package topg.interswitch_userservice.user.controller;

import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import topg.interswitch_userservice.user.dto.*;
import topg.interswitch_userservice.user.service.IUserService;
import topg.interswitch_userservice.user.service.UserServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

private final UserServiceImpl userService;

    // Get user by ID
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUserById(Authentication connectedUser) {
        UserResponseDto response = userService.getUserById(connectedUser);
        return ResponseEntity.ok(response);
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Soft delete user by ID
    @DeleteMapping("/delete/me")
    public ResponseEntity<String> deleteUser(Authentication connectedUser) {
        String result = userService.deleteUserById(connectedUser);
        return ResponseEntity.ok(result);
    }

    // Edit user (if implemented later)
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> editUser(
            @PathVariable String userId,
            @Valid @RequestBody UserRequest request
    ) {
        UserResponseDto response = userService.editUserById(userId, request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/me/deduct")
    public ResponseEntity<?> deductUserBalanceMe(
            Authentication connectedUser,
            @RequestBody DeductBalanceRequest request
    ) {
        try {
            userService.deductUserBalance(connectedUser, request.amount());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    @PostMapping("/topup")
    public ResponseEntity<UserResponseDto> topUpBalance(
            Authentication connectedUser,
            @RequestBody DeductBalanceRequest request
    ) {
        UserResponseDto response = userService.topUpBalance(connectedUser, request.amount());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<BalanceResponse> getBalance(Authentication connectedUser) {
        BalanceResponse response = userService.getBalance(connectedUser);
        return ResponseEntity.ok(response);
    }


}

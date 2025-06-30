package topg.interswitch_userservice.user.service;


import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import topg.interswitch_userservice.email.EmailService;
import topg.interswitch_userservice.email.EmailTemplate;
import topg.interswitch_userservice.enums.Role;
import topg.interswitch_userservice.exceptions.InvalidLoginDetailsException;
import topg.interswitch_userservice.exceptions.TokenNotFoundException;
import topg.interswitch_userservice.security.JwtUtils;
import topg.interswitch_userservice.security.UserDetailsImplService;
import topg.interswitch_userservice.token.model.Token;
import topg.interswitch_userservice.token.repository.TokenRepository;
import topg.interswitch_userservice.user.dto.*;
import topg.interswitch_userservice.user.model.User;
import topg.interswitch_userservice.user.repository.UserRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;


    private final JwtUtils jwtUtils;


    @Override
    @Transactional
    public UserResponseDto createUser(UserRequest userRequest) throws MessagingException {
        validateCustomerRequest(userRequest);

        User user = User.builder()
                .firstName(userRequest.firstName())
                .lastName(userRequest.lastName())
                .email(userRequest.email())
                .password(passwordEncoder.encode(userRequest.password())) // assuming you're encoding passwords
                .phoneNumber(userRequest.phoneNumber())
                .bvn(userRequest.bvn())
                .nin(userRequest.nin())
                .balance(userRequest.balance() != null ? userRequest.balance() : BigDecimal.ZERO)
                .role(Role.CUSTOMER)
                .is_active(false)
                .build();

        User savedUser = userRepository.save(user);
        sendValidationEmail(user);


        UserDto userDto = new UserDto(
                savedUser.getUserId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getBvn(),
                savedUser.getNin(),
                savedUser.getBalance(),
                savedUser.getCreatedAt(),
                savedUser.getUpdatedAt(),
                savedUser.is_active()
        );

        return new UserResponseDto(true, userDto);
    }


    public void sendValidationEmail(User user) throws MessagingException {
        var token = generateAndSaveToken(user);

        // Create the complete activation URL with token
        String baseUrl = "http://localhost:" + 8081;
        String completeActivationUrl = baseUrl + "/api/v1/auth/activate/" + token;


        emailService.sendEmail(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                EmailTemplate.ACTIVATE_ACCOUNT,
                completeActivationUrl, // Pass the complete URL instead of just the base URL
                token,
                "Account Verification"
        );
    }

    private void validateCustomerRequest(UserRequest request) {
        if (isBlank(request.email()) || isBlank(request.lastName()) ||
                isBlank(request.password()) || isBlank(request.phoneNumber()) ||
                isBlank(request.firstName()) || isBlank(request.bvn()) || isBlank(request.nin())) {
            throw new IllegalArgumentException("All fields must be provided and must not be empty.");
        }
    }

    private boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    private String generateAndSaveToken(User user) {
        String generateToken = generateActivationCode(32); // Increased length for URL safety
        Token token = Token.builder()
                .token(generateToken)
                .createdDate(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generateToken;
    }

    private String generateActivationCode(int length) {
        // Using URL-safe characters only
        String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        SecureRandom secureRandom = new SecureRandom();
        StringBuilder codeBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length());
            codeBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        return codeBuilder.toString();
    }

    @Override
    public UserResponseDto getUserById(Authentication connectedUser) {
        UserDetailsImplService userDetails = (UserDetailsImplService) connectedUser.getPrincipal();
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.is_active()) {
            throw new IllegalStateException("User with ID " + user.getUserId() + " is inactive.");
        }

        UserDto userDto = new UserDto(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getBvn(),
                user.getNin(),
                user.getBalance(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.is_active()
        );

        return new UserResponseDto(true, userDto);
    }


    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(user -> new UserDto(
                        user.getUserId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getBvn(),
                        user.getNin(),
                        user.getBalance(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        user.is_active()
                ))
                .toList();
    }


    @Override
    @Transactional
    public String deleteUserById(Authentication connectedUser) {
        UserDetailsImplService userDetails = (UserDetailsImplService) connectedUser.getPrincipal();
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!user.is_active()) {
            return "User is already inactive.";
        }

        user.set_active(false);
        userRepository.save(user);

        return "User deactivated successfully.";
    }


    @Override
    @Transactional
    public UserResponseDto editUserById(String userId, UserRequest userRequest) {
        Long id = Long.valueOf(userId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        if (!user.is_active()) {
            throw new IllegalStateException("Cannot edit an inactive user.");
        }

        // Check if 15 days have passed since account creation
        long daysSinceCreation = (System.currentTimeMillis() - user.getCreatedAt().getTime()) / (1000 * 60 * 60 * 24);
        if (daysSinceCreation < 15) {
            throw new IllegalStateException("User account cannot be edited until 15 days after creation.");
        }

        // Update only non-null and non-blank fields
        if (isNotBlank(userRequest.firstName())) user.setFirstName(userRequest.firstName());
        if (isNotBlank(userRequest.lastName())) user.setLastName(userRequest.lastName());
        if (isNotBlank(userRequest.phoneNumber())) user.setPhoneNumber(userRequest.phoneNumber());
        User updatedUser = userRepository.save(user);

        UserDto userDto = new UserDto(
                updatedUser.getUserId(),
                updatedUser.getFirstName(),
                updatedUser.getLastName(),
                updatedUser.getEmail(),
                updatedUser.getPhoneNumber(),
                updatedUser.getBvn(),
                updatedUser.getNin(),
                updatedUser.getBalance(),
                updatedUser.getCreatedAt(),
                updatedUser.getUpdatedAt(),
                updatedUser.is_active()
        );

        return new UserResponseDto(true, userDto);
    }

    private boolean isNotBlank(String input) {
        return input != null && !input.trim().isEmpty();
    }

    // Updated method to handle URL-based activation
    @Transactional
    public String activateAccountByToken(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new TokenNotFoundException("Invalid activation token"));

        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser());
            throw new RuntimeException("Activation link has expired. A new activation email has been sent to your email address");
        }

        var user = userRepository.findById(savedToken.getUser().getUserId())
                .orElseThrow(() -> new TokenNotFoundException("User not found"));

        if (user.is_active()) {
            return "Account is already activated";
        }

        user.set_active(true);
        userRepository.save(user);

        // Optionally, you can delete the token after successful activation to prevent reuse
        tokenRepository.delete(savedToken);

        return "Account activated successfully";
    }

    // Keep the old method for backward compatibility (if needed)
    public void activateUserToken(String token) throws MessagingException {
        activateAccountByToken(token);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        log.debug("Authentication attempt for email: {}", authenticationRequest.email());

        try {
            // Authenticate the user
            Authentication authentication = authenticateUser(authenticationRequest);
            log.debug("Authentication successful for email: {}", authenticationRequest.email());

            // Set authentication context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImplService userDetails = (UserDetailsImplService) authentication.getPrincipal();

            // Generate JWT token for the authenticated user
            String jwt = jwtUtils.generateToken(userDetails);
            log.debug("JWT token generated successfully for email: {}", authenticationRequest.email());

            return new AuthenticationResponse(true, jwt);
        } catch (BadCredentialsException ex) {
            log.error("Bad credentials for email: {}", authenticationRequest.email());
            throw new InvalidLoginDetailsException("Invalid username or password.");
        } catch (UsernameNotFoundException ex) {
            log.error("User not found or not active for email: {}", authenticationRequest.email());
            throw new InvalidLoginDetailsException("User not found or account not activated.");
        } catch (Exception ex) {
            log.error("Authentication failed for email: {} - Error: {}", authenticationRequest.email(), ex.getMessage());
            throw new InvalidLoginDetailsException("Login failed. Please try again.");
        }
    }

    private Authentication authenticateUser(AuthenticationRequest loginRequestDto) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.email(),
                        loginRequestDto.password()
                )
        );
    }


    public void deductUserBalance(Authentication connectedUser, BigDecimal amount) {
        String email = ((UserDetailsImplService) connectedUser.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.is_active()) {
            throw new IllegalStateException("User is not active");
        }

        BigDecimal currentBalance = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;

        if (currentBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        user.setBalance(currentBalance.subtract(amount));
        userRepository.save(user);
    }





    @Transactional
    @Override
    public UserResponseDto topUpBalance(Authentication connectedUser, BigDecimal amount) {
        UserDetailsImplService userDetails = (UserDetailsImplService) connectedUser.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.is_active()) {
            throw new IllegalStateException("User is not active");
        }

        user.setBalance(user.getBalance().add(amount));
        User updated = userRepository.save(user);

        UserDto dto = new UserDto(
                updated.getUserId(),
                updated.getFirstName(),
                updated.getLastName(),
                updated.getEmail(),
                updated.getPhoneNumber(),
                updated.getBvn(),
                updated.getNin(),
                updated.getBalance(),
                updated.getCreatedAt(),
                updated.getUpdatedAt(),
                updated.is_active()
        );

        return new UserResponseDto(true, dto);
    }







    @Override
    public BalanceResponse getBalance(Authentication connectedUser) {
        UserDetailsImplService userDetails = (UserDetailsImplService) connectedUser.getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.is_active()) {
            throw new IllegalStateException("User is not active");
        }

        return new BalanceResponse(user.getBalance());
    }


}
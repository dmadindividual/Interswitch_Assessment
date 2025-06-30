package topg.interswitch_userservice.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import topg.interswitch_userservice.user.model.User;
import topg.interswitch_userservice.user.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Attempting to load user with email: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            log.debug("User not found with email: {}", email);
            throw new UsernameNotFoundException("User with email " + email + " not found.");
        }

        log.debug("User found - Email: {}, Active: {}, Role: {}",
                user.getEmail(), user.is_active(), user.getRole());

        if (!user.is_active()) {
            log.debug("User account is not active for email: {}", email);
            throw new UsernameNotFoundException("User with email " + email + " is not active.");
        }

        log.debug("Successfully loaded user details for email: {}", email);

        // Return your custom UserDetailsImplService instead of Spring's User
        return new UserDetailsImplService(
                user.getEmail(),
                user.getPassword(),
                user.is_active(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
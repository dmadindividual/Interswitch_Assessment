package topg.interswitch_userservice.user.service;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import topg.interswitch_userservice.user.model.User;
import topg.interswitch_userservice.user.repository.UserRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserSchedulerService {

    private final UserRepository userRepository;
    private final UserServiceImpl userService;

    @Scheduled(cron = "0 0 * * * *")
    public void resendVerificationEmails() {
        log.info("Running scheduled task: resendVerificationEmails");

        List<User> usersToEmail = userRepository.findAll().stream()
                .filter(user -> !user.is_active())
                .toList();

        for (User user : usersToEmail) {
            try {
                log.info("Resending verification email to: {}", user.getEmail());
                userService.sendValidationEmail(user);
            } catch (MessagingException e) {
                log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage());
            }
        }

    }
}

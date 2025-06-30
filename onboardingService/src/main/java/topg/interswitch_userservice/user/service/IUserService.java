package topg.interswitch_userservice.user.service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import topg.interswitch_userservice.user.dto.BalanceResponse;
import topg.interswitch_userservice.user.dto.UserRequest;
import topg.interswitch_userservice.user.dto.UserDto;
import topg.interswitch_userservice.user.dto.UserResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface IUserService {

    UserResponseDto createUser(UserRequest userRequest) throws MessagingException;

    UserResponseDto getUserById(Authentication authentication);


    List<UserDto> getAllUsers();



    String deleteUserById(Authentication connectedUser);

    UserResponseDto editUserById(String Long, UserRequest userRequest);

    UserResponseDto topUpBalance(Authentication connectedUser, BigDecimal amount);

    BalanceResponse getBalance(Authentication connectedUser);
}

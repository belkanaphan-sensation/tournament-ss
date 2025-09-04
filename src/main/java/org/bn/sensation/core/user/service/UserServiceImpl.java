package org.bn.sensation.core.user.service;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.presentation.ChangePasswordRequest;
import org.bn.sensation.core.user.presentation.ForgotPasswordRequest;
import org.bn.sensation.core.user.repository.UserRepository;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.bn.sensation.core.user.service.mapper.UserDtoMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl extends BaseService<UserEntity, UserDto> implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            UserDtoMapper userDtoMapper,
            PasswordEncoder passwordEncoder) {
        super(userRepository, userDtoMapper);
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto findById(Long id) {
        return null;
//        return UserDtoMapper.INSTANCE.toUserDto(userRepository.findById(id).orElse(null));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request, UserDetails userDetails) {
        if (!passwordEncoder.matches(request.currentPassword(), userDetails.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Password are not the same");
        }
        UserEntity user =
                userRepository
                        .findByUsername(userDetails.getUsername())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "User with username not found: " + userDetails.getUsername()));
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);
    }

    @Override
    @Async
    public void sendEmail(ForgotPasswordRequest request) {
    }
}

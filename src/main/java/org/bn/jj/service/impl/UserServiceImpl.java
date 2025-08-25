package org.bn.jj.service.impl;

import lombok.RequiredArgsConstructor;
import org.bn.jj.dto.auth.ChangePasswordRequest;
import org.bn.jj.dto.auth.ForgotPasswordRequest;
import org.bn.jj.dto.user.UserDto;
import org.bn.jj.entity.User;
import org.bn.jj.mapper.UserMapper;
import org.bn.jj.repository.UserRepository;
import org.bn.jj.service.UserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto findById(Long id) {
        return UserMapper.INSTANCE.toUserDto(userRepository.findById(id).orElse(null));
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
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User with username not found: " + userDetails.getUsername()));
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userRepository.save(user);
    }


    @Override
    @Async
    public void sendEmail(ForgotPasswordRequest request) {

    }
}

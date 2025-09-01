package org.bn.sensation.service.impl;

import lombok.RequiredArgsConstructor;
import org.bn.sensation.auth.JwtTokenProvider;
import org.bn.sensation.dto.auth.AuthenticationRequest;
import org.bn.sensation.dto.auth.AuthenticationResponse;
import org.bn.sensation.dto.auth.RegisterRequest;
import org.bn.sensation.entity.User;
import org.bn.sensation.entity.enums.Status;
import org.bn.sensation.mapper.UserMapper;
import org.bn.sensation.repository.UserRepository;
import org.bn.sensation.service.AuthenticationService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        User user =
                userRepository
                        .findByUsername(request.username())
                        .or(() -> userRepository.findByEmail(request.username()))
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                String.format(
                                                        "User %s doesn't exists",
                                                        request.username())));
        String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole().name());
        return new AuthenticationResponse(token);
    }

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        User user =
                User.builder()
                        .username(request.username())
                        .password(passwordEncoder.encode(request.password()))
                        .email(request.email())
                        .role(UserMapper.INSTANCE.map(request.role()))
                        .status(Status.ACTIVE)
                        .build();
        User savedUser = userRepository.save(user);
        String token =
                jwtTokenProvider.createToken(savedUser.getUsername(), savedUser.getRole().name());
        return new AuthenticationResponse(token);
    }
}

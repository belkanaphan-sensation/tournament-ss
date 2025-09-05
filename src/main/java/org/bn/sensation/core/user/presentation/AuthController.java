package org.bn.sensation.core.user.presentation;

import org.bn.sensation.core.user.service.UserService;
import org.bn.sensation.core.user.service.dto.ChangePasswordRequest;
import org.bn.sensation.core.user.service.dto.ForgotPasswordRequest;
import org.bn.sensation.core.user.service.dto.RegistrationRequest;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegistrationRequest request) {
        UserDto created = userService.register(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Смена пароля")
    @PatchMapping(path = "/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.changePassword(request, userDetails);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Восстановление пароля")
    @PostMapping(path = "/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.sendEmail(request);
        return ResponseEntity.ok().build();
    }
}

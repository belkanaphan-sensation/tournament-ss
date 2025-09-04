package org.bn.sensation.core.user.presentation;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.bn.sensation.core.user.service.UserService;
import org.bn.sensation.core.user.service.dto.ChangePasswordRequest;
import org.bn.sensation.core.user.service.dto.ForgotPasswordRequest;
import org.bn.sensation.core.user.service.dto.RegistrationRequest;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication")
public class AuthController {

//    private final AuthenticationManager authManager;
//    private final HttpServletRequest request;
    private final UserService userService;

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegistrationRequest request) {
        UserDto created = userService.register(request);
        return ResponseEntity.ok(created);
    }
/*

    @Operation(summary = "Login user via formLogin",  description = "Returns session cookie JSESSIONID")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {

        UsernamePasswordAuthenticationToken authReq =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication auth = authManager.authenticate(authReq);

        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true);

        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    @Operation(summary = "Login (via formLogin)", description = "Returns session cookie JSESSIONID")
    @PostMapping("/login")
    public ResponseEntity<?> login() {
        // Spring Security сам обработает credentials и создаст сессию
        return ResponseEntity.ok(Map.of("message", "Login successful"));
    }

    @Operation(summary = "Logout", description = "Invalidate session")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }
*/

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

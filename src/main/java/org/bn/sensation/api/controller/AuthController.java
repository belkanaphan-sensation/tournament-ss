package org.bn.sensation.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.bn.sensation.dto.auth.AuthenticationRequest;
import org.bn.sensation.dto.auth.AuthenticationResponse;
import org.bn.sensation.dto.auth.RegisterRequest;
import org.bn.sensation.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authorization", description = "Authentication and authorization API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Login")
    @PostMapping(path = "/login")
    public ResponseEntity<?> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Register new user")
    @PostMapping(path = "/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }
}

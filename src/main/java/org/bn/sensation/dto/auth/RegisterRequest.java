package org.bn.sensation.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Schema(
                        description = "Username of the user",
                        example = "john_doe",
                        maxLength = 200,
                        minLength = 1)
                @NotEmpty
                @Size(max = 200)
                @JsonProperty("username")
                String username,
        @Schema(
                        description = "Password for the account",
                        example = "strong_password_123",
                        maxLength = 200,
                        minLength = 1)
                @NotEmpty
                @Size(max = 200)
                @JsonProperty("password")
                String password,
        @Schema(
                        description = "Email address of the user (optional)",
                        example = "john.doe@example.com")
                @Nullable
                @Email
                @JsonProperty("email")
                String email,
        @Schema(
                        description = "Role assigned to the user",
                        example = "ADMIN",
                        allowableValues = {"JUDGE", "ADMIN", "PARTICIPANT"})
                @NotNull
                @JsonProperty("role")
                UserRole role) {}

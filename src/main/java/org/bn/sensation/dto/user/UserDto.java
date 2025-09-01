package org.bn.sensation.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import org.bn.sensation.dto.auth.UserRole;

public record UserDto(
        @Schema(
                        description = "Username of the user",
                        example = "john_doe",
                        maxLength = 200,
                        minLength = 1)
                String username,
        @Schema(
                        description = "Email address of the user (optional)",
                        example = "john.doe@example.com")
                String email,
        @Schema(
                        description = "Role assigned to the user",
                        example = "ADMIN",
                        allowableValues = {"JUDGE", "ADMIN", "PARTICIPANT"})
                UserRole role) {}

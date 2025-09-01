package org.bn.sensation.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @Schema(description = "Current password", maxLength = 200, minLength = 1)
                @NotEmpty
                @Size(max = 200)
                @JsonProperty("current_password")
                String currentPassword,
        @Schema(description = "New password", maxLength = 200, minLength = 1)
                @NotEmpty
                @Size(max = 200)
                @JsonProperty("new_password")
                String newPassword,
        @Schema(description = "New password confirmation", maxLength = 200, minLength = 1)
                @NotEmpty
                @Size(max = 200)
                @JsonProperty("confirm_password")
                String confirmPassword) {}

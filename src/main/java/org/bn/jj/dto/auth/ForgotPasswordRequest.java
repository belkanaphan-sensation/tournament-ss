package org.bn.jj.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(

        @Schema(description = "Email address of the user (optional)", example = "john.doe@example.com")
        @Nullable
        @Size(max = 200)
        @JsonProperty("email")
        String email,

        @Schema(description = "Username of the user (optional)", example = "john_doe", maxLength = 200, minLength = 1)
        @Nullable
        @Size(max = 200)
        @JsonProperty("username")
        String username) {
}

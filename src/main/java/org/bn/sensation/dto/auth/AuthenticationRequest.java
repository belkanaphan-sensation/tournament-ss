package org.bn.sensation.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record AuthenticationRequest(
        @Schema(
                        description = "Username of the user",
                        example = "elena",
                        maxLength = 200,
                        minLength = 1)
                @NotEmpty
                @Size(max = 200)
                @JsonProperty("username")
                String username,
        @Schema(
                        description = "Password for the account",
                        example = "1",
                        maxLength = 200,
                        minLength = 1)
                @NotEmpty
                @Size(max = 200)
                @JsonProperty("password")
                String password) {}

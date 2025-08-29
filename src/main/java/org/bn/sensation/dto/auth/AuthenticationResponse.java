package org.bn.sensation.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthenticationResponse(

        @Schema(description = "Bearer token")
        @JsonProperty("access_token")
        String accessToken
) {
}

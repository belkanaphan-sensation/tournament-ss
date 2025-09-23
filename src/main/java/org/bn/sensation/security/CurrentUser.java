package org.bn.sensation.security;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
@Getter
public class CurrentUser {
    private final SecurityUser securityUser;

    public CurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("No authentication");
        }
        if (!(authentication.getPrincipal() instanceof SecurityUser)) {
            throw new IllegalStateException("Principal is not SecurityUser");
        }
        this.securityUser = (SecurityUser) authentication.getPrincipal();
    }
}

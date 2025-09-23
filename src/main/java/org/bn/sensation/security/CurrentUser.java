package org.bn.sensation.security;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Getter
@Component
@RequestScope
public class CurrentUser {
    private final SecurityUser securityUser;

    public CurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Authentication object is null or principal is null");
        }
        if (!(authentication.getPrincipal() instanceof SecurityUser)) {
            throw new IllegalStateException(
                    "Principal object is not instance of JwtUser,"
                            + " probably you are trying to access public resource,"
                            + " CurrentUser is not accessible for public resources");
        }
        this.securityUser = (SecurityUser) authentication.getPrincipal();
    }
}
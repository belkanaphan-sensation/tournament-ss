package org.bn.sensation.auth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bn.sensation.entity.User;
import org.bn.sensation.entity.enums.Status;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final String username;
    private final String password;
    @Getter
    private final String email;
    @Getter
    private final boolean isActive;
    private final Set<SimpleGrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return isActive;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;
    }

    public static UserDetails fromUser(User user) {
        return new SecurityUser(
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getStatus().equals(Status.ACTIVE),
                user.getRole().getPermissions().stream()
                        .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                        .collect(Collectors.toSet())
        );
    }
}

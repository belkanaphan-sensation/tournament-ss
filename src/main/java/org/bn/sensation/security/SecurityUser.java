package org.bn.sensation.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.user.entity.UserStatus;
import org.bn.sensation.core.user.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

    public static UserDetails fromUser(UserEntity user) {
        return new SecurityUser(
                user.getUsername(),
                user.getPassword(),
                user.getPerson().getEmail(),
                user.getStatus().equals(UserStatus.ACTIVE),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toSet()));
    }
}

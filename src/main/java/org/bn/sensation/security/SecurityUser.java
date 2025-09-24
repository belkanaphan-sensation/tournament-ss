package org.bn.sensation.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    private final String password;
    @Getter
    private final UserStatus status;
    @Getter
    private final Person person;
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
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.BLOCKED;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return status == UserStatus.ACTIVE;
    }

    public static UserDetails fromUser(UserEntity user) {
        return new SecurityUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus(),
                user.getPerson().toBuilder().build(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toSet()));
    }
}

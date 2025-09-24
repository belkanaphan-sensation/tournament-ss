package org.bn.sensation.security;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.bn.sensation.core.common.dto.EntityLinkDto;
import org.bn.sensation.core.common.entity.Person;
import org.bn.sensation.core.user.entity.Role;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SecurityUser implements UserDetails {

    private final String username;
    private final String password;
    private final Set<SimpleGrantedAuthority> authorities;

    @Getter
    private final Long id;
    @Getter
    private final UserStatus status;
    @Getter
    private final Person person;
    @Getter
    private final Set<Role> roles;
    @Getter
    private final Set<EntityLinkDto> organizations;

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
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                        .collect(Collectors.toSet()),
                user.getId(),
                user.getStatus(),
                user.getPerson().toBuilder().build(),
                user.getRoles(),
                user.getOrganizations().stream()
                        .map(o -> new EntityLinkDto(o.getId(), o.getName()))
                        .collect(Collectors.toSet()));
    }
}

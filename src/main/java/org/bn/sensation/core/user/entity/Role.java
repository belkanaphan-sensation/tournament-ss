package org.bn.sensation.core.user.entity;

public enum Role {
    SUPERADMIN,
    ADMIN,
    OCCASION_ADMIN,
    USER,
    READER


//  SUPERADMIN(Set.of(Permission.ADMIN_WRITE, Permission.ADMIN_READ)),
//  ADMIN(Set.of(Permission.ADMIN_READ)),
//  OCCASION_ADMIN(Set.of()),
//  USER(Set.of(Permission.USER_WRITE, Permission.USER_READ)),
//  READER(Set.of(Permission.USER_READ));
//
//  private final Set<Permission> permissions;

  //    public List<SimpleGrantedAuthority> getAuthorities() {
  //        var authorities = getPermissions()
  //                .stream()
  //                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
  //                .collect(Collectors.toList());
  //        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
  //        return authorities;
  //    }
}

package org.bn.jj.mapper;

import org.bn.jj.dto.auth.UserRole;
import org.bn.jj.dto.user.UserDto;
import org.bn.jj.entity.User;
import org.bn.jj.entity.enums.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "role", source = "role")
    UserDto toUserDto(User user);

    default UserRole map(Role role) {
        return switch (role) {
            case ADMIN, SUPERADMIN -> UserRole.ADMIN;
            case USER -> UserRole.JUDGE;
            case READER -> UserRole.PARTICIPANT;
        };
    }

    default Role map(UserRole userRole) {
        return switch (userRole) {
            case JUDGE -> Role.USER;
            case PARTICIPANT -> Role.READER;
            case ADMIN -> Role.ADMIN;
        };
    }
}

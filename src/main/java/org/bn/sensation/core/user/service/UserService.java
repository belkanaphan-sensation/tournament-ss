package org.bn.sensation.core.user.service;


import java.util.Optional;

import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.ChangePasswordRequest;
import org.bn.sensation.core.user.service.dto.CreateUserRequest;
import org.bn.sensation.core.user.service.dto.ForgotPasswordRequest;
import org.bn.sensation.core.user.service.dto.RegistrationRequest;
import org.bn.sensation.core.user.service.dto.UpdateUserRequest;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService extends BaseService<UserEntity, UserDto>{

    void changePassword(ChangePasswordRequest request, UserDetails userDetails);

    void sendEmail(ForgotPasswordRequest request);

    UserDto register(RegistrationRequest request);

    // CRUD operations
    Page<UserDto> findAll(Pageable pageable);

    UserDto create(CreateUserRequest request);

    UserDto update(Long id, UpdateUserRequest request);

    void deleteById(Long id);

    //todo: Удалить после перехода на нормальный продовый энкодер паролья
    @Override
    default Optional<UserDto> findById(Long id) {
        return BaseService.super.findById(id);
    }

    UserDto assignUserToOrganization(Long userId, Long organizationId);
}

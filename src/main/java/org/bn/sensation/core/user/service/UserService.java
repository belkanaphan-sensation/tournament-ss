package org.bn.sensation.core.user.service;


import org.bn.sensation.core.common.service.BaseService;
import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.service.dto.ChangePasswordRequest;
import org.bn.sensation.core.user.service.dto.ForgotPasswordRequest;
import org.bn.sensation.core.user.service.dto.RegistrationRequest;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService extends BaseService<UserEntity, UserDto>{

    void changePassword(ChangePasswordRequest request, UserDetails userDetails);

    void sendEmail(ForgotPasswordRequest request);

    UserDto register(RegistrationRequest request);
}

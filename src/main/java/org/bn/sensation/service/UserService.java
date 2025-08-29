package org.bn.sensation.service;

import org.bn.sensation.dto.auth.ChangePasswordRequest;
import org.bn.sensation.dto.auth.ForgotPasswordRequest;
import org.bn.sensation.dto.user.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    UserDto findById(Long id);

    void changePassword(ChangePasswordRequest request, UserDetails userDetails);

    void sendEmail(ForgotPasswordRequest request);
}

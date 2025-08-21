package org.bn.jj.service;

import org.bn.jj.dto.auth.ChangePasswordRequest;
import org.bn.jj.dto.auth.ForgotPasswordRequest;
import org.bn.jj.dto.user.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    UserDto findById(Long id);

    void changePassword(ChangePasswordRequest request, UserDetails userDetails);

    void sendEmail(ForgotPasswordRequest request);
}

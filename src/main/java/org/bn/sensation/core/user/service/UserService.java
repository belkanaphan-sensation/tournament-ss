package org.bn.sensation.core.user.service;

import org.bn.sensation.core.user.presentation.ChangePasswordRequest;
import org.bn.sensation.core.user.presentation.ForgotPasswordRequest;
import org.bn.sensation.core.user.service.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

  UserDto findById(Long id);

  void changePassword(ChangePasswordRequest request, UserDetails userDetails);

  void sendEmail(ForgotPasswordRequest request);
}

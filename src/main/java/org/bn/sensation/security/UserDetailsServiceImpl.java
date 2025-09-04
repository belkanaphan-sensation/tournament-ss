package org.bn.sensation.security;

import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity user =
        userRepository
            .findByUsername(username)
            .or(() -> userRepository.findByPersonEmail(username))
            .orElseThrow(
                () ->
                    new UsernameNotFoundException(
                        String.format("User %s doesn't exists", username)));
    return SecurityUser.fromUser(user);
  }
}

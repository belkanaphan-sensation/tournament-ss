package org.bn.sensation.security;

import org.bn.sensation.core.user.entity.UserEntity;
import org.bn.sensation.core.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Загрузка пользователя по имени: {}", username);
    
    UserEntity user =
        userRepository
            .findByUsername(username)
            .or(() -> {
                log.debug("Пользователь не найден по username={}, поиск по email", username);
                return userRepository.findByPersonEmail(username);
            })
            .orElseThrow(
                () -> {
                    log.warn("Пользователь не найден ни по username, ни по email: {}", username);
                    return new UsernameNotFoundException(
                        String.format("User %s doesn't exists", username));
                });
    
    log.debug("Пользователь найден: id={}, username={}", user.getId(), user.getUsername());
    return SecurityUser.fromUser(user);
  }
}

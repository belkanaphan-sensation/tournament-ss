package org.bn.sensation.auth;

import lombok.RequiredArgsConstructor;
import org.bn.sensation.entity.User;
import org.bn.sensation.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user =
                userRepository
                        .findByUsername(username)
                        .or(() -> userRepository.findByEmail(username))
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                String.format("User %s doesn't exists", username)));
        return SecurityUser.fromUser(user);
    }
}

package org.bn.sensation.core.user.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.user.entity.UserEntity;

public interface UserRepository extends BaseRepository<UserEntity> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByPersonEmail(String email);
}

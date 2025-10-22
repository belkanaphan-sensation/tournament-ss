package org.bn.sensation.core.user.repository;

import java.util.Optional;

import org.bn.sensation.core.common.repository.BaseRepository;
import org.bn.sensation.core.user.entity.UserEntity;

import jakarta.persistence.EntityNotFoundException;

public interface UserRepository extends BaseRepository<UserEntity> {
    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByPersonEmail(String email);

    Optional<UserEntity> findByPersonPhoneNumber(String phoneNumber);

    default UserEntity getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден: " + id));
    }
}

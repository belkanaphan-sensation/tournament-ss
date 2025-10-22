package org.bn.sensation.core.activityuser.service;

import java.util.function.Predicate;

import org.bn.sensation.core.activity.entity.ActivityEntity;
import org.bn.sensation.core.activityuser.entity.ActivityUserEntity;

import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class ActivityUserUtil {

    public static ActivityUserEntity getFromActivity(
            ActivityEntity activity,
            @Nullable Long userId,
            Predicate<ActivityUserEntity> predicate) {
        return activity
                .getUserAssignments()
                .stream()
                .filter(predicate)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Юзер %s не найден для активности %s %s".formatted(userId, activity.getId(), activity.getName())));
    }
}

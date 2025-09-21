package org.bn.sensation.core.common.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("М"),
    FEMALE("Ж");

    private final String displayName;
}

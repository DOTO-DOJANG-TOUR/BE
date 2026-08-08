package com.doto.domain.member.entity;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum MemberRole {
    USER(EnumSet.noneOf(Authority.class)),
    ADMIN(EnumSet.of(Authority.ADMIN_ACCESS));

    private final Set<Authority> authorities;

    MemberRole(Set<Authority> authorities) {
        this.authorities = Collections.unmodifiableSet(authorities);
    }

    public Set<Authority> getAuthorities() {
        return authorities;
    }
}

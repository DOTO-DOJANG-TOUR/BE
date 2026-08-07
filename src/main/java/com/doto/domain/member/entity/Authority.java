package com.doto.domain.member.entity;

/**
 * 실제 접근 제어에 쓰이는 세분화된 권한.
 *
 * <p>{@link MemberRole}은 사용자가 어떤 역할인지를 나타내고, {@code Authority}는 그 역할이
 * 실제로 무엇을 할 수 있는지를 나타낸다. Security 계층은 항상 {@code Authority} 단위로
 * 인가를 검사하고, {@code MemberRole}에 직접 의존하지 않는다.
 */
public enum Authority {
    ADMIN_ACCESS
}

package com.doto.global.security;

import com.doto.domain.member.entity.Member;
import com.doto.domain.member.entity.MemberRole;
import com.doto.domain.member.entity.MemberStatus;
import java.util.Collection;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomMemberDetails implements UserDetails {

    private final Long memberId;
    private final MemberStatus status;
    private final MemberRole role;

    public CustomMemberDetails(Member member) {
        this.memberId = member.getId();
        this.status = member.getStatus();
        this.role = member.getRole();
    }

    public Long getMemberId() {
        return memberId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.name()))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return String.valueOf(memberId);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == MemberStatus.ACTIVE;
    }

}

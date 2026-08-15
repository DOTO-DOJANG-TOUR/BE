package com.doto.global.security.jwt;

import com.doto.domain.member.entity.Member;
import com.doto.domain.member.repository.MemberRepository;
import com.doto.global.security.CustomMemberDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = JwtTokenProvider.resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            Long memberId = jwtTokenProvider.getMemberId(token);
            memberRepository.findById(memberId).ifPresent(this::authenticate);
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(Member member) {
        CustomMemberDetails principal = new CustomMemberDetails(member);
        // 토큰이 유효해도 그 사이 계정이 비활성화됐을 수 있어 상태를 다시 확인한다
        if (!principal.isEnabled()) {
            return;
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}

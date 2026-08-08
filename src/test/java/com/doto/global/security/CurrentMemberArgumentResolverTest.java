package com.doto.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.doto.domain.auth.exception.AuthException;
import com.doto.domain.member.entity.Member;
import java.lang.reflect.Method;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class CurrentMemberArgumentResolverTest {

    private final CurrentMemberArgumentResolver resolver = new CurrentMemberArgumentResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class 파라미터_지원_여부 {

        @Test
        void CurrentMember와_CustomMemberDetails_조합만_지원한다() throws NoSuchMethodException {
            MethodParameter supported = parameterOf("withCurrentMember", CustomMemberDetails.class);
            MethodParameter unsupported = parameterOf("withoutAnnotation", CustomMemberDetails.class);

            assertThat(resolver.supportsParameter(supported)).isTrue();
            assertThat(resolver.supportsParameter(unsupported)).isFalse();
        }
    }

    @Nested
    class 인증_정보_해석 {

        @Test
        void 인증된_사용자가_있으면_CustomMemberDetails를_반환한다() {
            Member member = Member.register("홍길동");
            CustomMemberDetails principal = new CustomMemberDetails(member);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
            );

            Object result = resolver.resolveArgument(null, null, null, null);

            assertThat(result).isEqualTo(principal);
        }

        @Test
        void 인증_정보가_없으면_예외를_던진다() {
            assertThatThrownBy(() -> resolver.resolveArgument(null, null, null, null))
                    .isInstanceOf(AuthException.class);
        }
    }

    private MethodParameter parameterOf(String methodName, Class<?> paramType) throws NoSuchMethodException {
        Method method = TestTarget.class.getDeclaredMethod(methodName, paramType);
        return new MethodParameter(method, 0);
    }

    private static final class TestTarget {

        void withCurrentMember(@CurrentMember CustomMemberDetails memberDetails) {
        }

        void withoutAnnotation(CustomMemberDetails memberDetails) {
        }
    }
}

package com.doto.global.security;

import com.doto.domain.auth.exception.AuthErrorCode;
import com.doto.global.api.CommonResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/** 인증되지 않은 요청을 CommonResponse 형식의 JSON으로 응답하는 진입점 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Spring MVC의 ObjectMapper Bean과 별개로, Security 필터 단계에서 직접 JSON을 씀
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        AuthErrorCode errorCode = AuthErrorCode.UNAUTHENTICATED;
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(CommonResponse.error(errorCode))
        );
    }

}

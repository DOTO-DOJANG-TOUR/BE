package com.doto.global.config;

import com.doto.global.api.CommonResponse;
import com.doto.global.error.CommonErrorCode;
import com.doto.global.error.ErrorCode;
import com.doto.global.security.CurrentMember;
import com.doto.global.swagger.ApiErrorCodeExamples;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

@Configuration
public class SwaggerConfig {

    public static final String BEARER_AUTH = "bearerAuth";
    private static final String COMMON_RESPONSE_SCHEMA = "CommonResponse";

    static {
        // @CurrentMember는 요청 파라미터로 노출하지 않는다
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentMember.class);
    }

    @Bean
    public OpenAPI dotoOpenApi() {
        ResolvedSchema resolvedSchema = ModelConverters.getInstance()
                .resolveAsResolvedSchema(new AnnotatedType(CommonResponse.class));

        Components components = new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
        components.addSchemas(COMMON_RESPONSE_SCHEMA, resolvedSchema.schema);
        resolvedSchema.referencedSchemas.forEach(components::addSchemas);

        return new OpenAPI()
                .servers(List.of(new Server().url("/").description("현재 서버")))
                .components(components)
                .info(new Info()
                        .title("DOTO API")
                        .description("DOTO 백엔드 API 명세서")
                        .version("v1"));
    }

    @Bean
    public OperationCustomizer errorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            addErrorExample(operation, CommonErrorCode.INVALID_INPUT);
            addErrorExample(operation, CommonErrorCode.MALFORMED_REQUEST);
            addErrorExample(operation, CommonErrorCode.INTERNAL_SERVER_ERROR);

            ApiErrorCodeExamples annotation = findDomainErrorCodes(handlerMethod);
            if (annotation != null) {
                for (Class<? extends ErrorCode> errorCodeType : annotation.value()) {
                    addErrorCodeType(operation, errorCodeType);
                }
            }
            return operation;
        };
    }

    private ApiErrorCodeExamples findDomainErrorCodes(HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();
        ApiErrorCodeExamples annotation = AnnotatedElementUtils.findMergedAnnotation(
                beanType,
                ApiErrorCodeExamples.class
        );
        if (annotation != null) {
            return annotation;
        }

        for (Class<?> interfaceType : beanType.getInterfaces()) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(
                    interfaceType,
                    ApiErrorCodeExamples.class
            );
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private void addErrorCodeType(Operation operation, Class<? extends ErrorCode> errorCodeType) {
        if (!errorCodeType.isEnum()) {
            throw new IllegalArgumentException("ErrorCode 타입은 enum이어야 합니다: " + errorCodeType.getName());
        }

        Object[] constants = errorCodeType.getEnumConstants();
        for (Object constant : constants) {
            addErrorExample(operation, (ErrorCode) constant);
        }
    }

    private void addErrorExample(Operation operation, ErrorCode errorCode) {
        ApiResponses responses = operation.getResponses();
        if (responses == null) {
            responses = new ApiResponses();
            operation.setResponses(responses);
        }

        String status = String.valueOf(errorCode.getStatus().value());
        ApiResponse response = responses.computeIfAbsent(
                status,
                key -> new ApiResponse().description(errorCode.getStatus().getReasonPhrase())
        );
        if (response.getContent() == null) {
            response.setContent(new Content());
        }

        MediaType mediaType = response.getContent().computeIfAbsent(
                "application/json",
                key -> new MediaType()
        );
        if (mediaType.getSchema() == null) {
            mediaType.setSchema(new Schema<>().$ref("#/components/schemas/" + COMMON_RESPONSE_SCHEMA));
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("isSuccess", false);
        body.put("code", errorCode.getCode());
        body.put("message", errorCode.getMessage());
        body.put("result", null);

        mediaType.addExamples(errorCode.getCode(), new Example()
                .summary(errorCode.getMessage())
                .value(body));
    }
}

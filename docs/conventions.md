# DOTO 백엔드 컨벤션

## 패키지 구조

도메인 중심 구조를 사용합니다.

```text
com.doto
├── domain
│   └── {domain}
│       ├── controller
│       ├── dto
│       ├── entity
│       ├── exception
│       ├── repository
│       └── service
│           └── UseCase 패턴 또는 CQRS 패턴
└── global
    ├── api
    ├── common
    ├── config
    ├── error
    ├── logging
    ├── swagger
    └── validation
```

초기에는 단일 모듈을 유지합니다. 배포 단위가 갈리거나 도메인 경계를 컴파일 타임에 강제할 필요가 생길 때 멀티모듈을 검토합니다.

## Service와 트랜잭션

- 도메인별 Service 구조는 **UseCase 패턴과 CQRS 패턴 중 하나를 선택**합니다.
- 같은 도메인에서 두 패턴을 중첩하지 않습니다. 예를 들어 `AuthUseCase`가 다시 `AuthCommandService`, `AuthQueryService`에 의존하는 구조는 만들지 않습니다.
- 선택한 패턴은 해당 도메인에서 일관되게 유지하고, 전환이 필요하면 구조 변경 이유와 테스트 영향 범위를 PR에 기록합니다.
- 별도 공통 어노테이션 없이 Spring 표준 `@Service`와 `@Transactional`을 명시합니다.
- 외부 API 호출과 파일 업로드처럼 오래 걸리는 I/O는 가능한 한 DB 트랜잭션 밖으로 분리합니다.
- `readOnly = true`는 Hibernate의 flush 동작을 줄이고 의도를 표현하는 장치입니다. 모든 DB 쓰기를 절대 차단하는 보안 경계로 간주하지 않습니다.
- `Service` 인터페이스와 `ServiceImpl` 구현체를 관성적으로 만들지 않습니다. 대체 구현이 실제로 존재하거나 외부 시스템과의 경계를 분리할 때만 인터페이스를 만듭니다.
- 클래스 이름에 `Impl`을 사용하지 않고 `PlaceQueryService`, `SignUpService`처럼 책임을 이름에 드러냅니다.

| 선택 기준 | UseCase 패턴 | CQRS 패턴 |
|---|---|---|
| 적합한 경우 | 회원가입·로그인처럼 행위별 정책과 의존성이 크게 다름 | 조회·상태 변경의 책임과 모델이 명확히 다름 |
| Controller 의존성 | `{Domain}UseCase` 하나 | `{Domain}CommandService`, `{Domain}QueryService` |
| 세부 클래스 | `SignUpService`, `SignInService` 등 행위 단위 | Command와 Query 책임 단위 |
| 트랜잭션 | 실제 행위를 수행하는 Service가 소유 | Query는 read-only, Command는 쓰기 트랜잭션 |

### CQRS 패턴

- QueryService는 클래스 레벨에 `@Transactional(readOnly = true)`를 적용합니다.
- CommandService는 클래스 레벨에 쓰기 `@Transactional`을 적용합니다.
- 조회 메서드를 CommandService에 추가하거나 상태 변경 메서드를 QueryService에 추가하지 않습니다.
- 조회 후 상태를 변경하는 하나의 유즈케이스는 CommandService가 조회와 변경을 같은 쓰기 트랜잭션 안에서 수행합니다.

```java
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PlaceQueryService {
    private final PlaceRepository placeRepository;
}

@Service
@Transactional
@RequiredArgsConstructor
public class PlaceCommandService {
    private final PlaceRepository placeRepository;
}
```

### UseCase 패턴

- `{Domain}UseCase`는 Controller가 사용하는 진입점이며 라우팅과 결과 조합만 담당합니다.
- 실제 비즈니스 규칙과 트랜잭션은 `SignUpService`처럼 행위 단위 Service가 소유합니다.
- UseCase에는 전용 공통 어노테이션을 만들지 않고 Spring의 `@Component`를 사용합니다.
- 행위별 Service를 나눌 이유가 없다면 억지로 클래스를 늘리지 않습니다.

인증 도메인은 다음처럼 분리할 수 있습니다.

```text
AuthController
  └── AuthUseCase
        ├── SignUpService
        ├── SignInService
        └── SignOutService
```

```java
@Component
@RequiredArgsConstructor
public class AuthUseCase {
    private final SignUpService signUpService;
    private final SignInService signInService;

    public AuthResponseDTO signUp(SignUpRequestDTO request) {
        return signUpService.signUp(request);
    }
}
```

## API와 DTO

- API prefix는 `/api/v1`을 사용합니다.
- Controller는 요청 검증, Service 위임, 공통 응답 변환만 담당합니다.
- DTO 클래스와 record는 대문자 `DTO` 접미사로 끝냅니다. `Dto`, `Request`, `Response`만 사용한 이름은 허용하지 않습니다.
- 요청 DTO는 `{의미}RequestDTO`, 응답 DTO는 `{의미}ResponseDTO` 형식을 사용합니다. 예: `SignUpRequestDTO`, `PlaceResponseDTO`
- Entity를 API 응답으로 직접 반환하지 않습니다.
- 하나의 Entity를 응답 DTO로 단순 변환할 때는 응답 DTO의 `from(entity)`를 사용합니다.
- 여러 Entity나 부가 값을 조합하는 변환이 복잡하거나 반복되면 Converter의 `toResponse(...)`로 분리합니다.
- 요청 DTO에 `toEntity()`를 두지 않습니다. 이 메서드는 연관 Entity 조회, 권한 확인과 중복 검증 없이 객체를 만들기 쉽습니다.
- Service가 선행 검증과 연관 Entity 조회를 마친 뒤 Entity의 `create(...)`, `register(...)` 같은 도메인 정적 팩토리를 호출합니다.
- Bean Validation 메시지는 클라이언트가 이해할 수 있는 한국어 문장으로 명시합니다.
- API의 일시 값은 UTC 기반 ISO 8601 문자열로 반환합니다. 예시는 `2026-08-02T09:30:00Z`입니다.
- Entity와 DTO의 `Instant`에는 `@JsonFormat`이나 전용 serializer를 붙이지 않습니다. Spring Boot의 기본 ISO 직렬화를 사용하고 사용자 시간대 표현은 클라이언트가 담당합니다.

```java
public record PlaceResponseDTO(Long id, String name) {
    public static PlaceResponseDTO from(Place place) {
        return new PlaceResponseDTO(place.getId(), place.getName());
    }
}
```

| 대상 | 메서드 |
|---|---|
| 응답 DTO 단일 원본 변환 | `from(entity)` |
| Entity 생성 | `create(...)`, `register(...)` |
| Value Object 생성 | `of(...)` |
| 여러 객체를 조합한 응답 | `Converter.toResponse(...)` |

`fromEntity()`처럼 타입명까지 메서드에 반복하지 않습니다. 매개변수 타입으로 원본이 이미 명확합니다. 같은 DTO에 변환 원본이 많아져 overload가 모호해지면 Converter로 분리합니다.

## Swagger

- 도메인의 `controller` 패키지는 `{Domain}Api` 인터페이스와 `{Domain}Controller` 구현체로 분리합니다.
- Swagger 어노테이션과 Spring MVC 메서드 매핑은 `{Domain}Api`에 작성하고 Controller는 해당 인터페이스를 구현합니다.
- `@Tag`, `@Operation`과 성공 `@ApiResponse`는 Api 인터페이스에만 둡니다.
- Controller는 요청 검증, UseCase 또는 Service 위임과 `CommonResponse` 변환만 담당합니다.
- 인증이 필요한 API에만 `@SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)`를 선언합니다. 공개 API에는 전역 JWT 요구사항을 적용하지 않습니다.
- `COMMON-400`과 `COMMON-500` 오류 예시는 모든 operation에 공통으로 자동 등록됩니다.
- 도메인 Api 인터페이스에 `@ApiErrorCodeExamples({PlaceErrorCode.class})`를 선언하면 해당 ErrorCode의 상태별 예시가 자동 등록됩니다.
- Swagger 문서화 코드가 Service, Entity와 도메인 로직에 들어가지 않도록 합니다.

```java
@Tag(name = "Place", description = "장소 API")
@ApiErrorCodeExamples({PlaceErrorCode.class})
public interface PlaceApi {

    @Operation(summary = "장소 단건 조회")
    @ApiResponse(responseCode = "200", description = "장소 조회 성공")
    @SecurityRequirement(name = SwaggerConfig.BEARER_AUTH)
    ResponseEntity<CommonResponse<PlaceResponseDTO>> getPlace(Long placeId);
}
```

## 공통 응답과 예외

- JSON 응답 body는 `CommonResponse<T>`로 통일합니다. bare `Response`는 의미가 너무 넓고 Servlet·HTTP 라이브러리 타입과 구분하기 어렵고, `ApiResponse`는 Swagger의 동명 어노테이션 타입과 충돌하므로 사용하지 않습니다.
- Controller와 API 인터페이스는 `ResponseEntity<CommonResponse<T>>`를 반환합니다.
- `CommonResponse`는 JSON 계약만 담당하고 HTTP status와 header는 `ResponseEntity`가 담당합니다.
- 성공 응답의 HTTP status, code와 message는 `SuccessCode`에 모아 관리합니다. 공통 200은 `CommonSuccessCode.OK`, 생성 201은 `CommonSuccessCode.CREATED`를 사용합니다.
- Service는 DTO 또는 유즈케이스 결과만 반환하며 `CommonResponse`와 `ResponseEntity`에 의존하지 않습니다.
- `200 OK`도 Controller 반환 타입의 일관성을 위해 `ResponseEntity.ok(...)`로 명시합니다.
- 생성은 `201 Created`, 응답 body가 정말 필요 없는 삭제는 `204 No Content`를 사용합니다. `204`에는 `CommonResponse` body를 넣지 않습니다.
- 도메인별 `{Domain}ErrorCode` enum이 `ErrorCode`를 구현합니다.
- 도메인별 `{Domain}Exception`이 추상 `DomainException`을 상속합니다. Service와 Entity는 범용 예외를 직접 만들지 않습니다.
- `GlobalExceptionHandler`가 `DomainException`을 받아 ErrorCode의 HTTP status와 `CommonResponse`로 변환합니다.
- 공통 성공 코드는 `SUCCESS-{HTTP_STATUS}` 형식을 사용합니다.
- 에러 코드는 `{DOMAIN}-{HTTP_STATUS}-{SEQUENCE}` 형식을 사용합니다.
- 내부 예외 메시지와 stack trace는 응답에 노출하지 않습니다.
- `401 Unauthorized`와 `403 Forbidden`, `404 Not Found`, `409 Conflict`를 의미에 맞게 구분합니다.

```java
public final class PlaceException extends DomainException {
    public PlaceException(PlaceErrorCode errorCode) {
        super(errorCode);
    }
}

return ResponseEntity.status(CommonSuccessCode.CREATED.getStatus())
        .body(CommonResponse.success(CommonSuccessCode.CREATED, PlaceResponseDTO.from(place)));
```

## 전용 Validator

- `@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Min`, `@Max`로 표현 가능한 검증은 표준 Bean Validation을 우선합니다.
- 재사용되는 단일 값 형식이나 두 필드 이상의 조합 검증에만 전용 Validator를 만듭니다.
- 공통 검증은 `global.validation`, 특정 도메인 규칙은 `domain.{domain}.validation`에 둡니다.
- 어노테이션은 `@ValidXxx`, 구현체는 `XxxValidator`로 이름을 맞춥니다.
- 단일 필드 Validator는 `null`을 유효한 값으로 처리하고 필수 여부는 `@NotNull`이 담당하게 합니다.
- 여러 필드의 관계를 검증할 때는 Request DTO 클래스 레벨에 어노테이션을 선언합니다.
- Validator에는 Repository, Service, 외부 API를 주입하지 않습니다. DB 조회, 권한, 중복과 상태 검증은 Service 또는 Domain 책임입니다.
- 검증 실패 메시지는 전용 어노테이션의 기본 메시지로 제공하고 필요한 경우 DTO에서 덮어쓸 수 있게 합니다.
- Validator는 경계값, `null`, 정상값, 실패값을 단위 테스트합니다.

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "시작일은 종료일보다 늦을 수 없습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

## Entity

- 테이블명은 복수형을 사용합니다.
- `Member`, `Place`처럼 API에서 직접 식별되고 다른 도메인에서 오래 참조하는 핵심 Aggregate Root는 PK 필드에 `@Id`, `@Tsid`를 직접 선언해 TSID를 사용합니다. 공통 상위 클래스로 감싸지 않고 각 Entity에 인라인으로 작성합니다.
- 단순 매핑, 이력, 로그처럼 외부 식별자가 필요 없는 보조 Entity에는 TSID를 강제하지 않습니다. PostgreSQL `IDENTITY` 또는 도메인에 맞는 복합키를 사용합니다.
- TSID 컬럼은 PostgreSQL `BIGINT`로 만들고 `IDENTITY`, sequence, 별도 default를 지정하지 않습니다. 컬럼명은 `{domain}_id` 형식(`member_id`, `place_id`)으로 각 Entity에서 바로 지정합니다. ID는 Hypersistence Utils의 `@Tsid` 식별자 생성기로 INSERT 이전에 발급합니다.
- TSID는 API 요청·응답에서 `String`으로 표현합니다. JavaScript의 안전한 정수 범위를 넘을 수 있으므로 JSON number로 반환하지 않습니다.
- TSID의 시간 정렬 특성은 조회·정렬 최적화에만 활용하며 생성 시각이나 보안 토큰으로 사용하지 않습니다. 생성 시각은 `createdAt`으로 관리합니다.
- 연관관계는 단방향과 지연 로딩을 기본으로 합니다.
- Entity 생성자는 JPA용 `protected` 기본 생성자와 외부에서 호출할 수 없는 생성자로 제한합니다.
- 생성 시 규칙이 있는 Entity는 `create`, `register`, `issue`처럼 유즈케이스를 드러내는 정적 팩토리를 공개 생성 진입점으로 사용합니다.
- Entity 클래스 레벨의 공개 `@Builder`는 사용하지 않습니다. 필수값 누락과 도메인 생성 경로 우회를 막기 어렵기 때문입니다.
- 선택 인자가 많아 생성자가 지나치게 복잡한 경우에만 private 내부 Builder를 허용하며, 정적 팩토리만 해당 Builder를 호출합니다.
- 단순 생성인데 정적 팩토리가 아무 규칙 없이 생성자를 감싸기만 한다면 생성자 사용도 허용합니다.
- 모든 필드 setter를 열지 않고 의미 있는 상태 변경 메서드를 제공합니다.
- 스키마 변경은 Entity 자동 생성이 아닌 Flyway migration으로만 반영합니다.
- `createdAt`, `updatedAt`처럼 사건이 발생한 시점은 `Instant`로 저장하고 PostgreSQL에서는 `TIMESTAMPTZ`를 사용합니다.

```java
@Entity
public class Place {
    protected Place() {
    }

    private Place(String name, PlaceStatus status) {
        this.name = name;
        this.status = status;
    }

    public static Place create(String name) {
        return new Place(name.trim(), PlaceStatus.ACTIVE);
    }
}
```

```java
@Entity
@Table(name = "members")
public class Member extends BaseTimeEntity {

    @Id
    @Tsid
    @Column(name = "member_id")
    private Long id;

    // 핵심 Aggregate Root만 선택적으로 TSID를 사용합니다.
}
```

## 로깅과 캐시

- 애플리케이션 코드는 SLF4J API를 사용하고 구현체는 Spring Boot 기본 Logback을 사용합니다.
- 모든 요청은 `X-Trace-Id`를 응답에 반환하며 로그 MDC의 `traceId`로 연결합니다.
- 기본 요청 로그에는 method, URI path, status, elapsed time만 기록합니다. 인증 헤더와 요청·응답 본문은 기록하지 않습니다.
- Caffeine은 단일 인스턴스 안에서만 유효한 로컬 캐시입니다. 정합성이 중요한 데이터에는 사용하지 않습니다.
- 캐시 이름, TTL, 최대 크기, 무효화 시점을 기능 PR에 명시합니다.

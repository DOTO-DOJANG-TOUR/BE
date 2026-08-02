# 테스트 전략

커버리지 비율보다 장애와 회귀를 막는 테스트를 우선합니다.

## TDD 진행 방식

TDD는 테스트 개수를 늘리는 기법보다 구현할 동작을 먼저 명세하고 설계 피드백을 빠르게 받는 방식으로 사용합니다.

1. Issue나 테스트 이름으로 Given-When-Then 시나리오를 먼저 확정합니다.
2. 가장 작은 의미 있는 실패 테스트를 작성합니다. 컴파일 실패도 Red 단계로 허용합니다.
3. 해당 테스트만 통과시키는 최소 구현을 작성합니다.
4. 전체 테스트가 Green인 상태에서 이름, 책임, 중복과 의존성을 리팩터링합니다.
5. 다음 성공 또는 실패 시나리오로 이동합니다.

레이어를 기계적으로 위에서 아래로 모두 테스트하지 않고 기능의 위험에 따라 첫 테스트 위치를 고릅니다.

| 변경 유형 | 첫 번째 실패 테스트 | 다음 테스트 |
|---|---|---|
| 상태 변경과 도메인 규칙 | 행위 단위 Service, CommandService 또는 Entity 테스트 | Controller 계약, 핵심 통합 흐름 |
| 검색, 정렬과 커스텀 조회 | PostgreSQL Repository 테스트 | 선택한 패턴의 조회 Service 테스트 |
| Validation과 HTTP 계약 | Controller slice 테스트 | 필요한 Service 테스트 |
| 트랜잭션, 캐시와 동시성 | 통합 테스트 | 경계 내부의 세부 단위 테스트 |
| 회귀 버그 | 버그를 재현하는 가장 가까운 테스트 | 수정 후 관련 상위 흐름 테스트 |

### 유즈케이스별 테스트 격리

- `SignUpService` 테스트는 회원가입에 필요한 협력 객체만 준비합니다. 로그인이나 로그아웃 의존성 변경 때문에 실패하지 않아야 합니다.
- `AuthUseCase`는 하위 서비스로 올바르게 위임하고 결과를 조합하는지만 검증합니다.
- Repository와 외부 API 같은 경계만 mock하고 Entity와 Value Object는 실제 객체를 사용합니다.
- 테스트하기 어려워 의존성이 계속 늘어난다면 mock을 더 추가하기 전에 Service 책임 분리를 검토합니다.
- Controller 테스트에서는 Service 내부 규칙을 반복하지 않고 path, status, Validation과 `CommonResponse` 계약을 검증합니다.
- 핵심 사용자 흐름은 단위 테스트와 별도로 Testcontainers 통합 테스트 한 건 이상으로 연결을 확인합니다.

## 우선순위

1. 핵심 도메인 규칙과 상태 전이의 성공·실패 분기
2. 권한, 존재 여부, 중복과 상태 충돌
3. API 요청 Validation과 공통 응답 계약
4. Flyway migration과 실제 PostgreSQL 호환성
5. 캐시 hit/miss, eviction과 트랜잭션 경계
6. 동시성 제어가 필요한 기능의 경쟁 조건

## 테스트 종류

- Unit: Spring context 없이 도메인 규칙을 빠르게 검증합니다.
- Controller slice: 상태 코드, Validation, `CommonResponse`, ErrorCode를 검증합니다.
- Repository: H2 대신 PostgreSQL Testcontainers에서 실제 쿼리를 검증합니다.
- Integration: 핵심 사용자 흐름과 계층 간 연결, Flyway 적용을 검증합니다.

테스트는 JUnit 5를 사용하고 성공·실패 또는 기능 단위로 `@Nested`를 구성합니다. 테스트 이름은 행위와 기대 결과가 드러나는 한국어 문장으로 작성합니다.

JaCoCo와 Codecov는 변경 추세와 테스트 사각지대를 찾는 보조 지표입니다. 초기에는 일괄 커버리지 하한을 두지 않으며, 핵심 비즈니스 코드가 생기면 changed-lines coverage를 기준으로 점진적으로 게이트를 검토합니다.

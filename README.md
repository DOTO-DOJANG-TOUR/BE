# DOTO Server

> 도장 투어 서비스 **DOTO**의 백엔드 저장소입니다.<br />
> Java 21과 Spring Boot를 기반으로 API와 도메인 로직, PostgreSQL 데이터를 관리합니다.
> 
---

## 🛠️ Tech Stack

<p>
  <strong>Language & Framework</strong><br />
  <img src="https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21" />
  <img src="https://img.shields.io/badge/Spring_Boot_4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 4" />
  <img src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white" alt="Gradle" />
</p>

<p>
  <strong>Persistence & Database</strong><br />
  <img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Data JPA" />
  <img src="https://img.shields.io/badge/PostgreSQL_17-4169E1?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 17" />
  <img src="https://img.shields.io/badge/Flyway-CC0200?style=for-the-badge&logo=flyway&logoColor=white" alt="Flyway" />
  <img src="https://img.shields.io/badge/TSID-4B5563?style=for-the-badge" alt="TSID" />
</p>

<p>
  <strong>Test & Quality</strong><br />
  <img src="https://img.shields.io/badge/JUnit_5-25A162?style=for-the-badge&logo=junit5&logoColor=white" alt="JUnit 5" />
  <img src="https://img.shields.io/badge/Testcontainers-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Testcontainers" />
  <img src="https://img.shields.io/badge/JaCoCo-B4A76C?style=for-the-badge" alt="JaCoCo" />
  <img src="https://img.shields.io/badge/Codecov-F01F7A?style=for-the-badge&logo=codecov&logoColor=white" alt="Codecov" />
</p>

<p>
  <strong>API & Observability</strong><br />
  <img src="https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black" alt="Swagger" />
  <img src="https://img.shields.io/badge/Actuator-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot Actuator" />
  <img src="https://img.shields.io/badge/SLF4J_&_Logback-EA4335?style=for-the-badge" alt="SLF4J and Logback" />
  <img src="https://img.shields.io/badge/Caffeine_Cache-6F4E37?style=for-the-badge" alt="Caffeine Cache" />
</p>

<p>
  <strong>Infra & CI/CD</strong><br />
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker" />
  <img src="https://img.shields.io/badge/Docker_Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker Compose" />
  <img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white" alt="GitHub Actions" />
</p>

---

## 📚 목차

1. [프로젝트 소개](#-프로젝트-소개)
2. [백엔드 팀원](#-백엔드-팀원)
3. [서버 구성](#-서버-구성)
4. [프로젝트 구조](#-프로젝트-구조)
5. [로컬 실행](#-로컬-실행)
6. [테스트 전략](#-테스트-전략)
7. [핵심 설계 원칙](#-핵심-설계-원칙)
8. [GitHub 관리 규칙](#-github-관리-규칙)

---

## 🧭 프로젝트 소개

**DOTO Server**는 도장 투어 서비스의 API와 핵심 비즈니스 규칙을 담당합니다.

현재는 도메인 기능을 안정적으로 확장할 수 있도록 다음 기반을 구성한 단계입니다.

- `/api/v1` 기반 REST API와 Swagger 문서
- Spring Data JPA와 PostgreSQL 기반 영속성
- Flyway를 이용한 스키마 변경 이력 관리
- 도메인 ErrorCode, Exception과 공통 응답 계약
- TraceId 기반 HTTP 요청 추적과 Logback 로깅
- Caffeine 기반 로컬 캐시
- JUnit 5와 Testcontainers 기반 테스트
- GitHub Actions와 Codecov 기반 CI

---

## 👥 백엔드 팀원

<div align="center">

| Backend | Backend |
|:---:|:---:|
| <img src="https://github.com/JiwonLee42.png" width="140" alt="JiwonLee42" /> | <img src="https://github.com/oculo0204.png" width="140" alt="oculo0204" /> |
| [JiwonLee42](https://github.com/JiwonLee42) | [oculo0204](https://github.com/oculo0204) |

</div>

---

## 🖥️ 서버 구성

```text
Client
  └── DOTO Server
        ├── Spring MVC API
        ├── Domain / Service
        ├── Spring Data JPA
        ├── Caffeine Local Cache
        └── PostgreSQL
```

초기에는 하나의 Spring Boot 애플리케이션으로 운영합니다. 배포 단위가 나뉘거나 도메인 경계를 컴파일 단계에서 강제할 필요가 생길 때 멀티모듈 전환을 검토합니다.

---

## 📂 프로젝트 구조

```text
src
├── main
│   ├── java/com/doto
│   │   ├── DotoApplication.java
│   │   ├── domain
│   │   │   └── {domain}
│   │   │       ├── controller
│   │   │       ├── dto
│   │   │       ├── entity
│   │   │       ├── exception
│   │   │       ├── repository
│   │   │       └── service       # UseCase 또는 Command/Query
│   │   └── global
│   │       ├── api          # 공통 응답
│   │       ├── common       # 공통 Entity 기반 클래스
│   │       ├── config       # JPA, Cache, Swagger, 시간 설정
│   │       ├── error        # 공통 예외 처리
│   │       ├── logging      # TraceId 요청 로깅
│   │       └── swagger      # Swagger 문서화 어노테이션
│   └── resources
│       ├── db/migration     # Flyway migration
│       ├── application.yml
│       ├── application-local.yml
│       ├── application-prod.yml
│       └── logback-spring.xml
└── test/java/com/doto
```

도메인 중심 패키지 구조를 사용하고, 도메인 내부에서 Controller, Service, Repository와 Entity의 책임을 분리합니다.

---

## 🚀 로컬 실행

### 요구 사항

- JDK 21
- Docker와 Docker Compose

### 실행 방법

```bash
docker compose -f docker-compose.yml up -d
./gradlew bootRun
```

`local` profile이 기본으로 활성화됩니다. 운영에서는 `SPRING_PROFILES_ACTIVE=prod`와 DB 접속 환경 변수를 반드시 주입합니다.

### 접속 주소

| 항목 | 주소 |
|---|---|
| Server | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |
| Application Health Check | `http://localhost:8080/health` |
| Health Check | `http://localhost:8080/actuator/health` |

### 환경 변수

| 환경 변수 | 로컬 기본값 | 설명 |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/doto` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `doto` | DB 사용자 |
| `DB_PASSWORD` | `doto` | DB 비밀번호 |
| `CACHE_MAXIMUM_SIZE` | `1000` | 캐시별 최대 항목 수 |
| `CACHE_EXPIRE_AFTER_WRITE` | `10m` | 캐시 기본 TTL |

---

## 🧪 테스트 전략

```bash
./gradlew clean test
```

H2 대신 Testcontainers의 `postgres:17-alpine`을 사용해 운영 DB와의 차이를 줄입니다. Docker가 제공되는 CI에서는 다음 항목을 검증합니다.

- 빈 PostgreSQL에서 Flyway migration이 정상 적용되는지
- 실제 쿼리, 인덱스와 제약 조건이 유효한지
- API Validation과 공통 응답 계약이 유지되는지
- 도메인 규칙의 성공·실패 분기가 보호되는지
- 캐시 무효화, 트랜잭션과 동시성 경계가 안전한지

Codecov 수치는 목표 그 자체가 아니라 테스트 사각지대를 찾는 신호로 사용합니다. 상세 기준은 [테스트 전략](docs/test-strategy.md)을 참고합니다.

JaCoCo 리포트는 테스트 실행 후 아래 경로에 생성됩니다.

| 리포트 | 경로 |
|---|---|
| HTML | `build/reports/jacoco/test/html/index.html` |
| XML | `build/reports/jacoco/test/jacocoTestReport.xml` |

---

## 🧩 핵심 설계 원칙

- API 응답 body는 `CommonResponse<T>`, HTTP status와 header는 `ResponseEntity`가 담당합니다.
- 성공 응답의 HTTP status, code와 message는 `SuccessCode`로 관리합니다.
- DTO 이름은 `SignUpRequestDTO`, `PlaceResponseDTO`처럼 대문자 `DTO` 접미사를 사용합니다. `Dto` 표기는 사용하지 않습니다.
- 도메인별 Service 구조는 UseCase 패턴과 CQRS 패턴 중 하나를 선택하며 두 패턴을 중첩하지 않습니다. `ServiceImpl` 네이밍은 사용하지 않습니다.
- 도메인별 `{Domain}ErrorCode`와 `{Domain}Exception`을 만들고 `@RestControllerAdvice`에서 처리합니다.
- Swagger는 공통 오류와 도메인 ErrorCode의 실제 응답 예시를 자동으로 문서화합니다.
- 요청마다 `X-Trace-Id`를 발급하거나 전달받아 MDC와 응답 헤더에 기록합니다.
- 단일 Entity 응답 변환은 DTO의 `from()`, Value Object 생성은 `of()`를 사용합니다.
- 복잡하거나 반복되는 응답 조합은 Converter의 `toResponse()`로 분리합니다.
- 요청 DTO에는 `toEntity()`를 두지 않고 Service의 선행 검증 후 Entity를 생성합니다.
- Entity 클래스 레벨의 공개 Builder 대신 도메인 정적 팩토리를 사용합니다.
- `User`, `Place`처럼 외부에서 오래 참조하는 핵심 Aggregate Root만 TSID를 사용합니다.
- 일시는 UTC `Instant`로 저장하고 API에서는 `2026-08-02T09:30:00Z` 형태의 ISO 8601 문자열로 반환합니다. 사용자 시간대 표시는 클라이언트가 담당합니다.
- 스키마 변경은 `ddl-auto`가 아닌 Flyway migration으로 관리합니다.
- Caffeine은 정합성이 덜 중요한 조회 데이터에만 제한적으로 사용합니다.

세부 규칙은 [DOTO 백엔드 컨벤션](docs/conventions.md)을 참고합니다.

---

## 🌿 GitHub 관리 규칙

### 브랜치

| 목적 | 브랜치 |
|---|---|
| 배포 기준 | `main` |
| 통합 개발 | `develop` |
| 기능 개발 | `feature/*` |
| 버그 수정 | `fix/*` |
| 초기 설정 | `init/*` |

- `main`과 `develop`에는 직접 push하지 않습니다.
- 작업 전 Issue를 만들고 Pull Request에서 관련 Issue를 연결합니다.
- Pull Request는 CI 통과와 코드리뷰 후 병합합니다.
- Issue Form 생성 시 작성자, 작업 유형, 중요도와 `D-n` 라벨을 자동으로 지정합니다.
- 열린 Issue의 D-day 라벨은 매일 KST 00:00에 갱신합니다.

자세한 정책은 [Issue 관리 규칙](docs/issue-management.md)을 참고합니다.

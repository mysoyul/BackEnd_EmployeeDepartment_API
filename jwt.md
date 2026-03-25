# JWT(JSON Web Token) 토큰 생성 구현 및 개선 가이드

## 1. JWT 개요

JWT는 서버와 클라이언트 사이에서 인증 정보를 안전하게 전달하기 위한 표준 토큰 형식입니다.
세션을 서버에 저장하지 않는 **무상태(Stateless)** 방식으로 동작합니다.

### JWT 구조

```
Header.Payload.Signature

eyJhbGciOiJIUzI1NiJ9  .  eyJzdWIiOiJhZG1pbi4uLiIsImlhdCI6Li4ufQ  .  SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
└── Base64(Header) ──┘  └──────── Base64(Payload) ─────────────────┘  └──── HMAC-SHA256(Header+Payload, 시크릿키) ────┘
```

| 부분 | 내용 |
|---|---|
| **Header** | 알고리즘(`HS256`), 토큰 타입(`JWT`) |
| **Payload** | Claims: `sub`(사용자 식별자), `iat`(발급 시간), `exp`(만료 시간) |
| **Signature** | Header + Payload를 시크릿 키로 서명한 값 — 위변조 방지 |

---

## 2. 사용 라이브러리

**jjwt 0.12.x** (`io.jsonwebtoken`)

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.7</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.7</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.7</version>
</dependency>
```

> jjwt 0.12.x는 0.11.x와 API가 다릅니다. `SignatureAlgorithm` enum이 제거되고
> `Jwts.SIG.HS256` 방식으로 변경되었습니다.

---

## 3. 코드 개선 내용

### 3-1. 개선 전/후 비교

| 항목 | 개선 전 | 개선 후 |
|---|---|---|
| 시크릿 키 위치 | 코드에 `public static final` 하드코딩 | `application.properties` + `@Value` 주입 |
| 키 접근 제어 | `public static` → 외부 직접 접근 가능 | `private` 메서드 `getSigningKey()` 로 캡슐화 |
| 키 생성 방식 | `SECRET.getBytes()` (ASCII 변환, 잘못된 방식) | `Decoders.BASE64.decode(secret)` (올바른 방식) |
| 만료 시간 위치 | 코드에 `public static final` 하드코딩 | `application.properties` + `@Value` 주입 |
| 미사용 임포트 | `SignatureAlgorithm`, `UUID`, `Key`, `Map`, `HashMap` | 모두 제거 |
| 반환 타입 | `isTokenExpired()` → `Boolean` (래퍼) | `boolean` (프리미티브) |
| 오타 | `exprireDate`, `exprired` | `expireDate`, `expired` |
| 세션 정책 | `SecurityConfig`에 정책 미설정 | `SessionCreationPolicy.STATELESS` 추가 |

### 3-2. 키 생성 방식 상세

```java
// ❌ 개선 전: SECRET.getBytes()
// hex 문자열을 그대로 ASCII 바이트로 변환 → 64바이트(512bit)의 ASCII 값 사용
// 의도한 키 값이 아님
public static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

// ✅ 개선 후: Decoders.BASE64.decode()
// Base64 문자열을 디코딩해 실제 바이너리 키 바이트(32byte=256bit)를 사용
private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
}
```

### 3-3. 시크릿 키 외부화

```properties
# application-prod.properties
jwt.secret=${JWT_SECRET:5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437}
jwt.expiration=${JWT_EXPIRATION:3600}
```

- `${JWT_SECRET:...}` 형식은 환경변수 `JWT_SECRET`이 있으면 그 값을, 없으면 기본값을 사용합니다.
- 운영 환경에서는 반드시 환경변수로 주입하여 시크릿 키가 코드/설정 파일에 노출되지 않도록 합니다.

---

## 4. 개선된 JwtService 핵심 코드

```java
@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;          // application.properties에서 주입

    @Value("${jwt.expiration}")
    private int expiration;         // 초 단위 (3600 = 60분)

    private static final SecureDigestAlgorithm<SecretKey, SecretKey> ALGORITHM = Jwts.SIG.HS256;

    // 시크릿 키를 private 메서드로 캡슐화 — 외부 노출 차단
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    // 토큰 생성
    public String generateToken(String userName) {
        Date expireDate = Date.from(Instant.now().plusSeconds(expiration));
        return Jwts.builder()
                .signWith(getSigningKey(), ALGORITHM)   // 서명 알고리즘: HS256
                .subject(userName)                      // sub 클레임: 사용자 이메일
                .issuedAt(new Date())                   // iat 클레임: 발급 시간
                .expiration(expireDate)                 // exp 클레임: 만료 시간
                .compact();
    }

    // 토큰 유효성 검증 (UserDetails 비교)
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    // 토큰 파싱 유효성 검증 (서명/만료 여부)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT was expired or incorrect", ex);
        }
    }
}
```

---

## 5. 인증 흐름

### 5-1. 토큰 발급 흐름

```
[클라이언트]
  POST /userinfos/login
  { "email": "admin@company.com", "password": "password" }
      │
      ▼
[UserInfoController.authenticateAndGetToken()]
      │
      ├─ AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)
      │       │
      │       └─ UserInfoUserDetailsService.loadUserByUsername(email)
      │               └─ UserInfoRepository.findByEmail(email) → UserInfo 조회
      │               └─ BCryptPasswordEncoder.matches(rawPw, encodedPw) 검증
      │
      └─ 인증 성공 → JwtService.generateToken(email)
              │
              ├─ Header: { alg: HS256 }
              ├─ Payload: { sub: email, iat: 현재시간, exp: 현재+3600초 }
              └─ Signature: HMAC-SHA256(Header+Payload, 시크릿키)
      │
      ▼
[클라이언트] JWT 토큰 수신 및 저장 (localStorage / sessionStorage)
```

### 5-2. 토큰 활용 흐름

```
[클라이언트]
  GET /api/employees
  Authorization: Bearer <JWT 토큰>
      │
      ▼
[JwtAuthenticationFilter]
      ├─ Authorization 헤더에서 토큰 추출
      ├─ jwtService.extractUsername(token) → 이메일 추출
      │       └─ 실패(만료/위변조) → 401 JSON 즉시 반환, 필터 체인 중단
      ├─ userDetailsService.loadUserByUsername(email) → DB에서 UserDetails 로드
      ├─ jwtService.validateToken(token, userDetails) → 토큰 유효성 검증
      └─ SecurityContextHolder에 인증 정보(authorities 포함) 설정
      │
      ▼
[SecurityConfig.exceptionHandling]
      ├─ 토큰 없이 인증 필요 경로 접근 → 401 JSON
      └─ 인증됐으나 권한 부족 → 403 JSON
      │
      ▼
[@PreAuthorize] 메서드 레벨 권한 검사
      │
      ▼
[EmployeeController] 정상 응답
```

---

## 6. API 사용법

### 6-1. 회원 가입 (토큰 불필요)

```
POST http://localhost:8080/userinfos/new
Content-Type: application/json

{
  "name": "Admin",
  "email": "admin@company.com",
  "password": "password",
  "roles": "ROLE_ADMIN"
}
```

### 6-2. 로그인 및 토큰 발급

```
POST http://localhost:8080/userinfos/login
Content-Type: application/json

{
  "email": "admin@company.com",
  "password": "password"
}
```

**응답:**
```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbi5...
```

### 6-3. 발급된 토큰 디코딩 확인

`jwt.io`에서 위 토큰을 붙여 넣으면 아래와 같이 디코딩됩니다.

```json
// Header
{ "alg": "HS256" }

// Payload
{
  "sub": "admin@company.com",
  "iat": 1742745600,
  "exp": 1742749200
}
```

---

## 7. Filter & SecurityConfig 개선 내용

### 7-1. JwtAuthenticationFilter 개선

| 항목 | 개선 전 | 개선 후 |
|---|---|---|
| 의존성 주입 | `@Autowired` 필드 주입 | `@RequiredArgsConstructor` 생성자 주입 |
| 토큰 파싱 예외 처리 | 예외 전파 → 500 Internal Server Error | try-catch → **401 JSON** 즉시 반환 |
| 권한 로그 레벨 | `log.info` (모든 요청마다 INFO 출력) | `log.debug` (필요 시에만 확인) |

**개선 전 — 예외 미처리:**
```java
// 만료/위변조 토큰 시 JwtException 발생 → 500 에러
username = jwtService.extractUsername(token);
```

**개선 후 — 예외 처리 및 401 반환:**
```java
try {
    username = jwtService.extractUsername(token);
} catch (Exception e) {
    log.warn("JWT token parsing failed: {}", e.getMessage());
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Invalid or expired JWT token\"}");
    return;  // 필터 체인 중단
}
```

### 7-2. SecurityConfig 개선

| 항목 | 개선 전 | 개선 후 |
|---|---|---|
| 의존성 주입 | `@Autowired` 필드 주입 | `@RequiredArgsConstructor` 생성자 주입 |
| 미인증 접근 응답 | Spring 기본 HTML 에러 페이지 | **401 JSON** |
| 권한 부족 응답 | Spring 기본 HTML 에러 페이지 | **403 JSON** |

**개선 후 — exceptionHandling 추가:**
```java
.exceptionHandling(ex -> ex
    // 토큰 없이 인증 필요 경로 접근 → 401
    .authenticationEntryPoint((request, response, e) -> {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"error\":\"Unauthorized\",\"message\":\"" + e.getMessage() + "\"}");
    })
    // 인증은 됐으나 권한 부족 → 403
    .accessDeniedHandler((request, response, e) -> {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"error\":\"Forbidden\",\"message\":\"" + e.getMessage() + "\"}");
    })
)
```

### 7-3. 에러 응답 시나리오 정리

| 상황 | 처리 위치 | HTTP 상태 | 응답 예시 |
|---|---|---|---|
| 토큰 만료/위변조 | JwtAuthenticationFilter (try-catch) | 401 | `{"error":"Unauthorized","message":"Invalid or expired JWT token"}` |
| 토큰 없이 인증 필요 경로 접근 | SecurityConfig authenticationEntryPoint | 401 | `{"error":"Unauthorized","message":"Full authentication is required"}` |
| ROLE 권한 부족 | SecurityConfig accessDeniedHandler | 403 | `{"error":"Forbidden","message":"Access Denied"}` |

---

## 8. DefaultExceptionAdvice 401 / 403 처리

### 8-1. 왜 DefaultExceptionAdvice에 추가하는가

Spring Security의 `SecurityConfig.exceptionHandling()`은 **필터 레벨** 예외만 처리합니다.
`@PreAuthorize`는 컨트롤러 메서드 호출 시 **AOP**로 동작하므로, 예외가 필터를 거치지 않고
`@RestControllerAdvice`로 전달됩니다.

```
@PreAuthorize 실패
    → AccessDeniedException / AuthenticationException 발생 (AOP)
    → @RestControllerAdvice (DefaultExceptionAdvice) 가 처리
    → SecurityConfig.exceptionHandling() 에는 도달하지 않음
```

### 8-2. 예외 클래스 계층

```
Throwable
└── Exception
    └── RuntimeException
        ├── org.springframework.security.access.AccessDeniedException   → 403
        └── org.springframework.security.core.AuthenticationException   → 401
            ├── BadCredentialsException          (비밀번호 불일치)
            ├── UsernameNotFoundException        (사용자 없음)
            ├── InsufficientAuthenticationException (미인증 접근)
            └── AuthenticationCredentialsNotFoundException (토큰 검증 실패)
```

> `@ExceptionHandler`는 **가장 구체적인 타입을 우선 매칭**합니다.
> `AccessDeniedException`과 `AuthenticationException` 전용 핸들러가 없으면
> `RuntimeException` 핸들러가 대신 처리하여 500이 반환됩니다.

### 8-3. 추가된 핸들러

```java
// 401 — 인증 실패
@ExceptionHandler(AuthenticationException.class)
protected ResponseEntity<ErrorObject> handleAuthenticationException(AuthenticationException e) {
    ErrorObject errorObject = new ErrorObject();
    errorObject.setStatusCode(HttpStatus.UNAUTHORIZED.value());
    errorObject.setMessage(e.getMessage());
    log.warn("Authentication failed: {}", e.getMessage());
    return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
}

// 403 — 권한 부족
@ExceptionHandler(AccessDeniedException.class)
protected ResponseEntity<ErrorObject> handleAccessDeniedException(AccessDeniedException e) {
    ErrorObject errorObject = new ErrorObject();
    errorObject.setStatusCode(HttpStatus.FORBIDDEN.value());
    errorObject.setMessage(e.getMessage());
    log.warn("Access denied: {}", e.getMessage());
    return new ResponseEntity<>(errorObject, HttpStatus.FORBIDDEN);
}
```

### 8-4. DefaultExceptionAdvice 전체 핸들러 처리 우선순위

| 우선순위 | 예외 타입 | HTTP 상태 | 발생 상황 |
|---|---|---|---|
| 1 | `ResourceNotFoundException` | 417 / 404 | 리소스 없음 |
| 2 | `HttpMessageNotReadableException` | 400 | 잘못된 요청 바디 |
| 3 | `MethodArgumentNotValidException` | 400 | 입력값 검증 실패 |
| 4 | `AuthenticationException` | **401** | 인증 실패 (자격증명 오류, 미인증) |
| 5 | `AccessDeniedException` | **403** | 권한 부족 (`@PreAuthorize` 실패) |
| 6 | `RuntimeException` | 500 | 그 외 모든 런타임 예외 |

### 8-5. 401 / 403 처리 경로 전체 정리

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 상황                        처리 위치                  상태
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 만료/위변조 토큰             JwtAuthenticationFilter    401
 토큰 없이 보호 경로 접근     SecurityConfig entryPoint  401
 로그인 자격증명 오류         DefaultExceptionAdvice     401
 @PreAuthorize 권한 부족      DefaultExceptionAdvice     403
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

## 9. 주의사항

| 항목 | 내용 |
|---|---|
| 시크릿 키 길이 | HS256 최소 256bit (32byte). `Decoders.BASE64.decode()` 결과가 32byte 이상이어야 함 |
| 시크릿 키 노출 | 절대 Git에 커밋 금지. 운영 환경에서는 `JWT_SECRET` 환경변수로 주입 |
| 토큰 저장 위치 | React 클라이언트에서 `localStorage` 또는 `sessionStorage`에 저장 |
| Authorization 헤더 | API 요청 시 `Authorization: Bearer <token>` 형식으로 전송 |
| 토큰 만료 | 기본 3600초(60분). 만료 후 재로그인 필요 (`/userinfos/login` 재호출) |
| Stateless | `SessionCreationPolicy.STATELESS`로 서버 세션 미생성 — 수평 확장에 유리 |

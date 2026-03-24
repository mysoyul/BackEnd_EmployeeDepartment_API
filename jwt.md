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

### 5-2. 토큰 활용 흐름 (다음 단계)

```
[클라이언트]
  GET /api/employees
  Authorization: Bearer <JWT 토큰>
      │
      ▼
[JwtAuthFilter] ← 아직 미구현 (추후 추가 필요)
      ├─ Authorization 헤더에서 토큰 추출
      ├─ JwtService.validateToken(token) 검증
      ├─ JwtService.extractUsername(token) → 사용자 이메일 추출
      └─ SecurityContextHolder에 인증 정보 설정
      │
      ▼
[EmployeeController] 정상 응답
```

> **현재 상태**: 토큰 발급(`/userinfos/login`)은 완성되었으나, API 요청 시 토큰을 검증하는
> `JwtAuthFilter`가 아직 구현되지 않았습니다. `/api/**` 엔드포인트에 JWT 인증을 적용하려면
> `OncePerRequestFilter`를 상속한 필터를 작성하고 `SecurityConfig`의 필터 체인에 등록해야 합니다.

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

## 7. 주의사항

| 항목 | 내용 |
|---|---|
| 시크릿 키 길이 | HS256 최소 256bit (32byte). `Decoders.BASE64.decode()` 결과가 32byte 이상이어야 함 |
| 시크릿 키 노출 | 절대 Git에 커밋 금지. 운영 환경에서는 `JWT_SECRET` 환경변수로 주입 |
| 토큰 저장 위치 | React 클라이언트에서 `localStorage` 또는 `sessionStorage`에 저장 |
| Authorization 헤더 | API 요청 시 `Authorization: Bearer <token>` 형식으로 전송 |
| 토큰 만료 | 기본 3600초(60분). 만료 후 재로그인 필요 (`/userinfos/login` 재호출) |
| Stateless | `SessionCreationPolicy.STATELESS`로 서버 세션 미생성 — 수평 확장에 유리 |

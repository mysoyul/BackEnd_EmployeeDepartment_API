package net.restapi.emp.security.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import net.restapi.emp.security.filter.JwtAuthenticationFilter;
import net.restapi.emp.security.userinfo.UserInfoUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

/**
 * Spring Security 전역 보안 설정 클래스
 *
 * 주요 역할:
 *   - HTTP 요청별 인증/인가 규칙 정의 (permitAll / authenticated)
 *   - JWT 무상태(Stateless) 세션 정책 설정
 *   - JwtAuthenticationFilter를 필터 체인에 등록
 *   - 인증/권한 실패 시 JSON 에러 응답 처리
 *   - DaoAuthenticationProvider (DB 기반 인증) 빈 등록
 *
 * 어노테이션:
 *   @EnableWebSecurity  - Spring Security 활성화
 *   @EnableMethodSecurity - @PreAuthorize, @PostAuthorize 메서드 레벨 보안 활성화
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // JwtAuthenticationFilter: 요청마다 JWT 토큰을 검증하는 커스텀 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // BCryptPasswordEncoder: 비밀번호 암호화/검증에 사용 (PasswordEncoderConfig에서 빈 등록)
    private final PasswordEncoder passwordEncoder;

    /**
     * HTTP 보안 필터 체인 설정
     *
     * 요청 처리 순서:
     *   1. JwtAuthenticationFilter (토큰 추출 및 SecurityContext 인증 등록)
     *   2. UsernamePasswordAuthenticationFilter (폼 로그인 - 미사용)
     *   3. authorizeHttpRequests (경로별 인증/인가 검사)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // REST API는 CSRF 토큰이 불필요 (쿠키/세션 미사용)
                .csrf(csrf -> csrf.disable())
                // CORS 활성화: CorsConfigurationSource 빈(prod) 또는 MVC CORS 설정(local)을 자동 참조
                // Spring Security 필터 체인 내에서 preflight(OPTIONS) 요청을 처리하므로
                // FilterRegistrationBean보다 먼저 실행되는 문제를 해결
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
                    // 인증 없이 접근 가능한 공개 경로
                    auth.requestMatchers("/api/employees/welcome", "/userinfos/new", "/userinfos/login").permitAll()
                            // /api/** 하위 경로는 모두 인증 필요
                            // 메서드 레벨 @PreAuthorize로 추가 권한(ROLE) 검사
                            .requestMatchers("/api/**").authenticated();
                })
                // JWT는 서버에 세션을 저장하지 않는 무상태(Stateless) 방식
                // STATELESS 설정 시 Spring Security가 HttpSession을 생성하거나 사용하지 않음
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // authenticationEntryPoint: 토큰 없이 인증 필요 경로 접근 시 호출 → 401 JSON 반환
                        // 기본값은 HTML 에러 페이지이므로 REST API용 JSON 응답으로 교체
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"error\":\"Unauthorized\",\"message\":\"" + e.getMessage() + "\"}");
                        })
                        // accessDeniedHandler: 인증은 됐으나 권한(ROLE) 부족 시 호출 → 403 JSON 반환
                        // 필터 레벨 AccessDeniedException 처리 (컨트롤러 레벨은 DefaultExceptionAdvice 처리)
                        .accessDeniedHandler((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write(
                                    "{\"error\":\"Forbidden\",\"message\":\"" + e.getMessage() + "\"}");
                        })
                )
                // DB 기반 인증 프로바이더 등록
                .authenticationProvider(authenticationProvider())
                // JwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 앞에 삽입
                // → 폼 로그인 필터보다 먼저 JWT 토큰을 검증하여 SecurityContext에 인증 정보 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * UserDetailsService 빈 등록
     * UserInfoUserDetailsService: 이메일로 DB를 조회하여 UserDetails 반환
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return new UserInfoUserDetailsService();
    }

    /**
     * DaoAuthenticationProvider 빈 등록
     *
     * 로그인(/userinfos/login) 시 AuthenticationManager가 사용하는 인증 프로바이더
     *   - UserDetailsService로 DB에서 사용자 조회
     *   - PasswordEncoder(BCrypt)로 비밀번호 일치 여부 검증
     */
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    /**
     * AuthenticationManager 빈 등록
     * UserInfoController의 로그인 처리에서 직접 주입받아 사용
     * AuthenticationConfiguration이 내부적으로 등록된 AuthenticationProvider를 조합하여 반환
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

}
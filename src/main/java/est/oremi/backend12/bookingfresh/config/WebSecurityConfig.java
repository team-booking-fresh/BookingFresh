package est.oremi.backend12.bookingfresh.config;

import est.oremi.backend12.bookingfresh.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity // -> http 랑 다른게 뭘까
                .csrf(auth -> auth.disable())
                // csrf 비활성화 -> JWT 환경에선 불필요? -> 세션을 사용하지 않기 때문에
                .httpBasic(AbstractHttpConfigurer::disable)
                // http basic 인증 비활성화 -> 사용자면  비밀번호를 텍스트로 전송하는 기본적인 인증 방식,
                // 보안에 취약하므로 JWT 롸 같이 암호화된 토큰 기반의 인증 방식을 사용할 때는 비활성화
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) // -> JWT 를 사용하기 때문에 세션을 생성하거나 사용하지 않도록 설정(stateless)
                .authorizeHttpRequests(auth ->   // 인증, 인가 설정
                        auth
                                .requestMatchers(
                                        "/h2-console/**",
                                        // 페이지 요청, 페이지 요청만 인증이 필요한 페이지도 등록
                                        "/signup",
                                        "/login",
                                        "/home",
                                        "index",
                                        // api 요청, 인증이 필요한 API는 여기에 올리면 안됨
                                        "/api/signup",   // POST /api/signup (회원가입 처리)
                                        "/api/login",     // POST /api/login (로그인 처리)
                                        // 토큰 재발급 처리,이미 AT 가 만료된 상황이기 때문에 인증 처리 없이 permit
                                        "/api/auth/refresh"
                                ).permitAll()
                                .requestMatchers(
                                        // === Secure Pages (SSR) === 조건부 페이지 랜더링
                                        // PageController가 RT 쿠키로 직접 보안 처리
                                        "/mypage", "/mypage/**",
                                        "/chat",
                                        "/ai",
                                        "/cart",
                                        "/products", "/",
                                        "/products/*", // 상품 상세 페이지
                                        "/order/**"
                                ).permitAll()
                                .requestMatchers(
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html" //Swagger 관련 URL
                                ).permitAll()
                                .requestMatchers(HttpMethod.GET, "/products", "/products/*").permitAll()
                                .requestMatchers("/static/**", "/css/**", "/js/**").permitAll() // 정적 리소스 접근 가능하게
                            // === 상품 관련 (GET만 허용) ===
                            .requestMatchers(HttpMethod.GET, "/products", "/products/*").permitAll()
                            .requestMatchers("/products/*/like").authenticated() // 좋아요는 인증 필요

                            // === 주문 관련 ===
                            .requestMatchers(
                                "/orders/create",           // POST - 주문 생성
                                "/orders/*/complete",       // PATCH - 주문 완료
                                "/orders/*/update-delivery", // PATCH - 배송 정보 수정
                                "/orders/*/cancel",         // PATCH - 주문 취소
                                "/orders/*",                // GET - 주문 조회
                                "/orders/my"                // GET - 내 주문 목록
                            ).authenticated() // 인증 필요

                            // === 장바구니 관련 ===
                            .requestMatchers("/api/cart/**").authenticated()

                            // === 사용자 정보 관련 ===
                            .requestMatchers("/api/me").authenticated()

                            // === SSR 페이지 (조건부 렌더링) ===
                            .requestMatchers(
                                "/mypage", "/mypage/**",
                                "/chat",
                                "/ai",
                                "/cart",
                                "/products",
                                "/",
                                "/products/*",
                                "/order/**"  // 주문 페이지
                            ).permitAll() // 페이지 자체는 접근 가능 (RT 쿠키로 PageController에서 확인)

                            // === 정적 리소스 ===
                            .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                                .anyRequest().authenticated()

                )
                // 토큰 필터 먼저 적용
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);;

        return httpSecurity.build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // 비밀번호 검증
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}

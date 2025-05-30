package capston.new_valance.config;

import capston.new_valance.jwt.JwtAuthenticationEntryPoint;
import capston.new_valance.jwt.JwtFilter;
import capston.new_valance.jwt.JwtUtil;
import capston.new_valance.oauth2.CustomFailHandler;
import capston.new_valance.oauth2.CustomOAuth2UserService;
import capston.new_valance.oauth2.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Lazy
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtUtil jwtUtil;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailHandler customFailHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager() {
        InMemoryOAuth2AuthorizedClientService clientService =
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
        OAuth2AuthorizedClientRepository clientRepository =
                new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(clientService);

        OAuth2AuthorizedClientProvider provider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .build();

        DefaultOAuth2AuthorizedClientManager clientManager =
                new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, clientRepository);
        clientManager.setAuthorizedClientProvider(provider);
        return clientManager;
    }

    @Bean
    public OAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
        );
    }

    // API 요청에 대한 별도 SecurityFilterChain (JWT 검증만 적용, OAuth2 로그인 제외)
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .cors(cors -> cors.configurationSource(corsConfig()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 공개 API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login/**", "/api/ai/**").permitAll()
                        .anyRequest().authenticated()
                )

                // formLogin 완전 비활성화 (302 방지)
                .formLogin(AbstractHttpConfigurer::disable)

                // BasicAuth → 401 챌린지용으로 다시 켜주기
                .httpBasic(withDefaults())

                // JWT 필터
                .addFilterBefore(new JwtFilter(jwtUtil),
                        UsernamePasswordAuthenticationFilter.class)

                // 인증 실패 시 401 리턴하도록 엔트리 포인트 지정
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
        ;

        return http.build();
    }



    // 나머지 웹(회원가입, OAuth2 로그인 등)에 대한 SecurityFilterChain
    @Bean
    @Order(2)
    public SecurityFilterChain oauth2SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfig()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/actuator/**",
                                        "/api/login/**",
                                        "/api/ai/metadata",
                                        "/oauth2/**",
                                        "/auth/refresh",
                                        "/custom-login",
                                        "/"
                                )
                                .permitAll()
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(endpoint -> endpoint.userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
                        .failureHandler(customFailHandler))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(jwtAuthenticationEntryPoint));

        return http.build();
    }


    // CORS 설정
    private CorsConfigurationSource corsConfig() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            // 허용 Origin 목록 확장
            config.setAllowedOrigins(List.of(
                    "http://localhost:3000",
                    "http://localhost:8000",
                    "https://new-valance-server.o-r.kr",
                    "https://loadbalancer-799709838.ap-northeast-2.elb.amazonaws.com"
            ));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setExposedHeaders(List.of("Authorization"));
            config.setAllowCredentials(true);
            config.setMaxAge(3600L);
            return config;
        };
    }

}

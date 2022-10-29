package me.chulgil.spring.meeting.infra.config;


import lombok.RequiredArgsConstructor;
import me.chulgil.spring.meeting.modules.account.AccountService;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;
import java.util.function.Supplier;

/**
 * SpringSecurity를 이용하기 위한 설정 클래스
 * 로그인 처리 시 파라미터, 화면 이동과 인증 처리로 데이터 접근처를 설정 한다.
 */
@Configuration
@EnableWebSecurity     // 스프링 Security Filter가 Spring Fileter Chain에 등록됨
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final AccountService accountService;
    private final DataSource dataSource;

    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .mvcMatchers("/", "/login", "/sign-up", "/check-email-token",
                        "/email-login", "/login-by-email", "/search/meeting").permitAll()
                .mvcMatchers(HttpMethod.GET, "/profile/*").permitAll()
                .anyRequest().authenticated()
        ;

        http.formLogin() //Form 로그인 인증 기능이 작동함
                .loginPage("/login") //사용자 정의 로그인 페이지
                .permitAll();  //사용자 정의 로그인 페이지 접근 권한 승인

        http.logout()
                .logoutSuccessUrl("/");

        http.rememberMe()
                .userDetailsService(accountService)
                .tokenRepository(tokenRepository());
    }

    @Bean
    public PersistentTokenRepository tokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }


    /**
     * Security 설정을 무시하는 요청을 설정
     * 정적 리소스(image,javascript,css)를 인가 처리 대상에서 제외 한다.
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .mvcMatchers("/node/node_modules/**")
                .antMatchers("/favicon.ico", "/resources/**", "/error")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
        /**
         * 기타 사용 예재
         * .antMatchers("/images/**")
         * .antMatchers("/css/**")
         * .antMatchers("/javascript/**")
         * .antMatchers("/js/**")
         *　.antMatchers("/images/**","/css/**");
         */
    }


//    @Bean
//    @Order(0)
//    SecurityFilterChain resources(HttpSecurity http) throws Exception {
//        http
//                .requestMatchers((matchers) -> matchers.antMatchers("/static/**"))
//                .authorizeHttpRequests((authorize) -> authorize.anyRequest().permitAll())
//                .requestCache().disable()
//                .securityContext().disable()
//                .sessionManagement().disable();
//
//        return http.build();
//    }


//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // 해당 기능을 사용하기 위해서는 프론트단 Form 에서 csrf토큰값 보내줘야함
//        // <input type="hidden" name="${_csrf.paremeterName }" value="${_csrf.token }"/>
//        http.csrf().disable(); // Spring Security의 SCRF를 막음. Post가 안될 경우가 존재하면 막는 경우도 있음.
//
//        http.authorizeHttpRequests() //
//                // 로그인 하지 않고 모두 권한 가짐
//                .antMatchers("/login", "/logout", "/sign-up").permitAll()
//                // 그 외 모든 요청은 인증 접근
//                .anyRequest().authenticated()
//        // .anyRequest().permitAll() // 로그인 하지 않고 모두 권한을 가짐.
//        ;
//
//        http.requiresChannel()
//                // .antMatchers("/**").requiresSecure() // https 로 리다이렉스 시킴
//                .antMatchers("/**").requiresInsecure() // http 로 리다이렉스 시킴
//        ;
//
//        http.formLogin()
//                .loginPage("/login")                          // Login 화면
//                .loginProcessingUrl("/loginProcess.do")          // Login 프로세스
//                // .defaultSuccessUrl("/main.do", true)
//                .successHandler(new CustomAuthenticationSuccessHandler("/main.do")) // 인증에 성공하면 Main 페이지로 Redirect
//                // // .failureHandler(new CustomAuthenticationFailureHandler("/login-fail")) // 커스텀 핸들러를 생성하여 등록하면 인증실패
//                // 후
//                .failureUrl("/login.do?fail=true") // 인증이 실패 했을 경우 이동하는 페이지를 설정합니다.
//                .usernameParameter("userId")                    // Login ID 명칭지정 - MemberRepository 의 id와 매칭됨.
//                .passwordParameter("password")                  // Login PW 명칭지정
//        ;
//
//        http.logout()
//                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//                .logoutSuccessUrl("/login") // 로그아웃에 성공하면 페이지 Redirect
//                .invalidateHttpSession(true) // Session 초기화
//        ;
//    }
//
//
//    // https://errorsfixing.com/generic-http-security-path-matcher-reference-matched-path-in-security-expression-2/
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
//                        .mvcMatchers(HttpMethod.POST, "/{resource}").access(new ResourceAuthorizationManager("create"))
//                        .mvcMatchers(HttpMethod.PUT, "/{resource}/{id}").access(new ResourceAuthorizationManager("update"))
//                        .anyRequest().authenticated()
//                )
//                .formLogin(Customizer.withDefaults());
//        return http.build();
//    }
//
//
//    public static final class ResourceAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
//
//        private final String action;
//
//        public ResourceAuthorizationManager(String action) {
//            this.action = action;
//        }
//
//        @Override
//        public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
//            AuthorizationManager<RequestAuthorizationContext> delegate =
//                    AuthorityAuthorizationManager.hasAuthority(createAuthority(context));
//            return delegate.check(authentication, context);
//        }
//
//        private String createAuthority(RequestAuthorizationContext context) {
//            String resource = context.getVariables().get("resource");
//            return String.format("%s:%s", this.action, resource);
//        }
//    }


}





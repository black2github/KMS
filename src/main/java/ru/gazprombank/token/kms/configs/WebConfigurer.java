package ru.gazprombank.token.kms.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import ru.gazprombank.token.kms.util.HeadersSpyFilter;

@Configuration
// добавляется DelegatingFilterProxy, его задача заключается в том,
// чтобы вызвать цепочку фильтров (FilterChainProxy) из Spring Security.
@EnableWebSecurity
// позволяет указать одну или несколько аннотаций безопасности, которые будут применены на уровне методов.
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = false)
// @EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class WebConfigurer {

    @Bean
    // public SecurityFilterChain filterChain(HttpSecurity http, HeadersSpyFilter headersSpyFilter) throws Exception {
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .requestMatchers("/keys/**")
                .hasAnyRole("ADMIN", "MASTER")
                //  .permitAll()
                .requestMatchers("/tokens/**")
                .hasRole("USER")
                .requestMatchers("/url1/**").permitAll()
                .requestMatchers("/url2/**").permitAll()
                .requestMatchers("/url3/**").permitAll()
                .and()
                //.formLogin(form -> form.loginPage("/login").permitAll())
                .logout().permitAll();
        // http.formLogin();
        http.httpBasic();
        // Выключаем защиту CSRF
        http.csrf().disable();
        // http.addFilterAfter(headersSpyFilter, BasicAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Relax Security for Public Resources
    // @Bean
    // public WebSecurityCustomizer ignoreResources() {
    //     return (webSecurity) -> webSecurity
    //             .ignoring()
    //             .requestMatchers("/public/*");
    // }

    //
    // Configure Authentication
    //
    @Bean
    public UserDetailsService userDetailsService(BCryptPasswordEncoder bCryptPasswordEncoder) {
    // public UserDetailsService userDetailsService() {

        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();

        manager.createUser(User.withUsername("user")
                .password(bCryptPasswordEncoder.encode("{noop}userPass"))
                .roles("USER")
                .build());
        manager.createUser(User.withUsername("admin")
                .password(bCryptPasswordEncoder.encode("{noop}adminPass"))
                .roles("USER", "ADMIN")
                .build());
        manager.createUser(User.withUsername("master")
                .password(bCryptPasswordEncoder.encode("{noop}masterPass"))
                .roles("MASTER", "USER", "ADMIN")
                .build());

        return manager;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
                                             UserDetailsService userDetailsService) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }
}


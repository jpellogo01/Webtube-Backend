package com.Webtube.site.Security;

import com.Webtube.site.Security.jwt.AuthEntryPointJwt;
import com.Webtube.site.Security.jwt.AuthTokenFilter;
import com.Webtube.site.Security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity

public class WebSecurityConfig { // extends WebSecurityConfigurerAdapter {
  @Autowired
  UserDetailsServiceImpl userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }


  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
      DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
       
      authProvider.setUserDetailsService(userDetailsService);
      authProvider.setPasswordEncoder(passwordEncoder());
   
      return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/api/v1/login").permitAll()
                            .requestMatchers("/api/v1/public-news").permitAll()
                            .requestMatchers("/api/v1/public-news").permitAll()
                            .requestMatchers("/api/v1/news/view/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/news/views/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/news/comment/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/news/comments/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/news").hasAnyRole("ADMIN", "AUTHOR")
                            .requestMatchers("/api/v1/user/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/rejected").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/approved").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/approve").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/pending").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/notifications/**").hasRole("AUTHOR")
                            .anyRequest().authenticated()
            );

    // Define your custom authentication provider if needed
    http.authenticationProvider(authenticationProvider());

    // Add the JWT authentication filter
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

}

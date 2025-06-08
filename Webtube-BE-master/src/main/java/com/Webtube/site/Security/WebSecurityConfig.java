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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
    http.csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth ->
                    auth.requestMatchers("/api/v1/login").permitAll()
                            .requestMatchers("/hitOpenaiApi/**").permitAll()
                            .requestMatchers("/api/v1/public-news/**").permitAll()
                            .requestMatchers("/api/v1/contribute-news/**").permitAll()
                            .requestMatchers("/api/v1/news-contribute/**").permitAll()
                            .requestMatchers("/api/v1/pubview-news/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/lic-news/**\").permitAll()\n" + ".requestMatchers(\"/api/v1/news/views/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/news/comments/pending").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/comments/all").hasRole("ADMIN")
                            .requestMatchers("/api/v1/delete-comment").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/comment/{action}/{commentID}").hasRole("ADMIN")
                            .requestMatchers("/api/v1/user-delete-comment/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/comment-news/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/comment-reply/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/news/comments/**").permitAll() // fixed pattern here
                            .requestMatchers("/api/v1/news").hasAnyRole("ADMIN", "AUTHOR")
                            .requestMatchers("/api/v1/AInews").hasAnyRole("ADMIN", "AUTHOR")
                            .requestMatchers("/api/v1/user/**").hasRole("ADMIN")
                            .requestMatchers("/api/facebook/post/**").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/rejected").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/reject").hasRole("ADMIN")
                            .requestMatchers("/api/v1/news/approved/comments/**").permitAll()
                            .requestMatchers("/api/v1/approved/news").hasRole("ADMIN")
                            .requestMatchers("/api/v1/approve/news").hasRole("ADMIN")
                            .requestMatchers("/api/v1/pending/news").hasRole("ADMIN")
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

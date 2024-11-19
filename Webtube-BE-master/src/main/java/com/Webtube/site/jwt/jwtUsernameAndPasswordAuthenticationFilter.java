//package com.Webtube.site.jwt;
//
//import com.Webtube.site.Model.User;
//import com.Webtube.site.Repository.UserRepository;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.BadCredentialsException;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.Date;
//
//public class jwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
//
//    private  final AuthenticationManager authenticationManager;
//    private final UserRepository userRepository; // Assuming you have a UserRepository
//
//    public jwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
//        this.authenticationManager = authenticationManager;
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//        try {
//            UsernameAndPasswordAuthenticationRequest authenticationRequest = new ObjectMapper()
//                    .readValue(request.getInputStream(), UsernameAndPasswordAuthenticationRequest.class);
//
//            // Fetch user details from the database
//            User user = userRepository.findByUsername(authenticationRequest.getUsername());
//            if (user == null || !user.getPassword().equals(authenticationRequest.getPassword())) {
//                throw new BadCredentialsException("Invalid username or password");
//            }
//
//            Authentication authentication = new UsernamePasswordAuthenticationToken(
//                    user.getUsername(),
//                    user.getPassword(),
//                    AuthorityUtils.createAuthorityList(user.getRole().toString()));
//
//            return authenticationManager.authenticate(authentication);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    @Override
//    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
//        String key = "securesecuresecuresecuresecuresecuresecuresecuresecure";
//        String token = Jwts.builder()
//                .setSubject(authResult.getName())
//                .claim("authorities", authResult.getAuthorities())
//                .setIssuedAt(new Date())
//                .setExpiration(java.sql.Date.valueOf(LocalDate.now().plusWeeks(2)))
//                        .signWith(Keys.hmacShaKeyFor(key.getBytes()))
//                                .compact();
//
//        response.addHeader("Authorization","Bearer " + token);
//    }
//}

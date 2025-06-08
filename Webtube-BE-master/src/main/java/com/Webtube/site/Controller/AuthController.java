package com.Webtube.site.Controller;

import com.Webtube.site.Model.ERole;
import com.Webtube.site.Model.Role;
import com.Webtube.site.Model.Users;
import com.Webtube.site.Repository.UsersRepository;
import com.Webtube.site.payload.request.LoginRequest;
import com.Webtube.site.payload.request.SignupRequest;
import com.Webtube.site.payload.response.JwtResponse;
import com.Webtube.site.payload.response.MessageResponse;
import com.Webtube.site.Repository.RoleRepository;
import com.Webtube.site.Security.jwt.JwtUtils;
import com.Webtube.site.Security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000/")
@RestController
@RequestMapping("/api/v1")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UsersRepository usersRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @PostMapping("/login")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtUtils.generateJwtToken(authentication);
    
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();    
    List<String> roles = userDetails.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .collect(Collectors.toList());

    return ResponseEntity.ok(new JwtResponse(
                         jwt,
                         userDetails.getId(),
                         userDetails.getFullname(),
                         userDetails.getUsername(),
                         userDetails.getEmail(), 
                         roles));
  }

}

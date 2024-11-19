package com.Webtube.site.Controller;

import com.Webtube.site.Exception.UsersNotFoundException;
import com.Webtube.site.Model.ERole;
import com.Webtube.site.Model.Role;
import com.Webtube.site.Model.Users;
import com.Webtube.site.Repository.RoleRepository;
import com.Webtube.site.Repository.UsersRepository;
import com.Webtube.site.payload.request.SignupRequest;
import com.Webtube.site.payload.response.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/v1")
public class UsersController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UsersRepository usersRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;


    // GET Mapping for retrieving all users
    // Modify the @GetMapping method in your backend to format the roles correctly
    @GetMapping("/user")
    public ResponseEntity<?> getAllUsers() {
        List<Users> usersList = usersRepository.findAll();
        if (usersList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new MessageResponse("No users found."));
        }

        List<Map<String, Object>> userDetailsList = usersList.stream().map(user -> {
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("fullname", user.getFullname());
            userDetails.put("username", user.getUsername());
            userDetails.put("email", user.getEmail());
            userDetails.put("password", user.getPassword()); // Be careful exposing passwords!

            // Add role names to response
            Set<String> roleNames = user.getRoles().stream()
                    .map(role -> role.getName().name()) // Ensure role name is properly converted
                    .collect(Collectors.toSet());
            userDetails.put("roles", roleNames);

            return userDetails;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(userDetailsList);
    }

    @PostMapping("/user")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (usersRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

//        if (usersRepository.existsByEmail(signUpRequest.getEmail())) {
//            return ResponseEntity
//                    .badRequest()
//                    .body(new MessageResponse("Error: Email is already in use!"));
//        }

        // Create new user's account
        Users user = new Users(signUpRequest.getFullname(),
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_AUTHOR)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found. 1"));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                if (role.equals("admin")) {
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(adminRole);
                } else {
                    Role authorRole = roleRepository.findByName(ERole.ROLE_AUTHOR)
                            .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                    roles.add(authorRole);
                }
            });
        }

        user.setRoles(roles);
        usersRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    // GET Mapping for retrieving user details by ID
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new UsersNotFoundException("Error: User with ID " + id + " not found."));
        return ResponseEntity.ok().body(user);
    }


    // PUT Mapping for updating user details
    @PutMapping("/user/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody SignupRequest updateRequest) {
        return usersRepository.findById(id).map(user -> {
            user.setUsername(updateRequest.getUsername());
            user.setEmail(updateRequest.getEmail());
            user.setPassword(encoder.encode(updateRequest.getPassword()));
            usersRepository.save(user);
            return ResponseEntity.ok(new MessageResponse("User updated successfully!"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Error: User not found.")));
    }

    // DELETE Mapping for deleting a user by ID
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        return usersRepository.findById(id).map(user -> {
            usersRepository.delete(user);
            return ResponseEntity.ok(new MessageResponse("User deleted successfully!"));
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Error: User not found.")));
    }
}



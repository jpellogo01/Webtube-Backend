package com.Webtube.site.Security;

import com.Webtube.site.Model.ERole;
import com.Webtube.site.Model.Role;
import com.Webtube.site.Model.Users;
import com.Webtube.site.Repository.RoleRepository;
import com.Webtube.site.Repository.UsersRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DefaultUserConfig {

    @Bean
    CommandLineRunner initDefaultUser(UsersRepository usersRepository,
                                      RoleRepository roleRepository,
                                      PasswordEncoder encoder) {
        return args -> {
            // Check if default user exists
            if (!usersRepository.existsByUsername("defaultUser")) {
                Users defaultUser = new Users("Admin Fullname", "defaultUser", "default@example.com", encoder.encode("password123"));
                Role defaultRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("Error: Default role is not found."));
                defaultUser.setRoles(Set.of(defaultRole));
                usersRepository.save(defaultUser);
                System.out.println("Default user created successfully.");
            }
        };
    }
}

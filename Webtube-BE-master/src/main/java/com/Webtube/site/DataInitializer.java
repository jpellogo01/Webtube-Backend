package com.Webtube.site;

import com.Webtube.site.Model.ERole;
import com.Webtube.site.Model.Role;
import com.Webtube.site.Repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if the roles already exist, if not, insert them
        if (roleRepository.findByName(ERole.ROLE_AUTHOR).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_AUTHOR));
        }
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
    }
}

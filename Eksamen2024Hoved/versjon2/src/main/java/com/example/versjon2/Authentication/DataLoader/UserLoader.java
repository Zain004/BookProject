package com.example.versjon2.Authentication.DataLoader;

import com.example.versjon2.Authentication.Repository.UserRepository;
import com.example.versjon2.Authentication.UserEntity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {
        if(userRepository.count() == 0) {
            Set<User.Role> roles = new HashSet<>();
            roles.add(User.Role.ADMIN);
            User user = new User("test.user-1", "Test123!", roles);
            user.updateHashPassword("Test123!", passwordEncoder);
            userRepository.save(user);
        }

    }


}

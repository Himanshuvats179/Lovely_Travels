package com.example.demo.repository;

import com.example.demo.entity.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {
   // Optional<UserLogin> findByEmail(String email);
    Optional<UserLogin> findByJwtToken(String token);
    Optional<UserLogin> findByUserId(Long userId);

}

package com.example.demo.repository;

import com.example.demo.entity.Users.User;
import com.example.demo.entity.Users.UserLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.ScopedValue;
import java.util.Optional;

public interface UserLoginRepository extends JpaRepository<UserLogin, Long> {
   // Optional<UserLogin> findByEmail(String email);
    Optional<UserLogin> findByJwtToken(String token);
    Optional<UserLogin> findByUserId(Long userId);

    Optional<Object> findByRefreshToken(String token);

    ScopedValue<UserLogin> findByUser(User user);

    void deleteByJwtToken(String jwtToken);
}

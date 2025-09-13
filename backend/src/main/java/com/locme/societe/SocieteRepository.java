package com.locme.societe;

import com.locme.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocieteRepository extends JpaRepository<Societe, Long> {
    Optional<Societe> findByUser(User user);
    Optional<Societe> findByEmail(String email);
    boolean existsByEmail(String email);
}

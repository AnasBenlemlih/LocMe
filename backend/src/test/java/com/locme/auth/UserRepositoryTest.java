package com.locme.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setNom("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setMotDePasse("encodedPassword");
        testUser.setRole(Role.CLIENT);
        testUser.setTelephone("1234567890");
        testUser.setAdresse("123 Main St");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test save and find user by ID")
    void testSaveAndFindById() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getNom());
        assertEquals("john@example.com", foundUser.get().getEmail());
        assertEquals(Role.CLIENT, foundUser.get().getRole());
    }

    @Test
    @DisplayName("Test find user by email")
    void testFindByEmail() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("john@example.com");

        // Then
        assertTrue(foundUser.isPresent());
        assertEquals("John Doe", foundUser.get().getNom());
        assertEquals("john@example.com", foundUser.get().getEmail());
    }

    @Test
    @DisplayName("Test find user by email when user does not exist")
    void testFindByEmailNotFound() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(foundUser.isPresent());
    }

    @Test
    @DisplayName("Test exists by email when user exists")
    void testExistsByEmailTrue() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("john@example.com");

        // Then
        assertTrue(exists);
    }

    @Test
    @DisplayName("Test exists by email when user does not exist")
    void testExistsByEmailFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(exists);
    }

    @Test
    @DisplayName("Test find all users")
    void testFindAll() {
        // Given
        User user1 = new User();
        user1.setNom("User 1");
        user1.setEmail("user1@example.com");
        user1.setMotDePasse("password1");
        user1.setRole(Role.CLIENT);

        User user2 = new User();
        user2.setNom("User 2");
        user2.setEmail("user2@example.com");
        user2.setMotDePasse("password2");
        user2.setRole(Role.SOCIETE);

        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        var users = userRepository.findAll();

        // Then
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user1@example.com")));
        assertTrue(users.stream().anyMatch(u -> u.getEmail().equals("user2@example.com")));
    }

    @Test
    @DisplayName("Test delete user")
    void testDeleteUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        Long userId = savedUser.getId();

        // When
        userRepository.delete(savedUser);
        entityManager.flush();

        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    @DisplayName("Test update user")
    void testUpdateUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        savedUser.setNom("Updated Name");
        savedUser.setTelephone("9876543210");

        // When
        User updatedUser = userRepository.save(savedUser);
        entityManager.flush();

        // Then
        Optional<User> foundUser = userRepository.findById(updatedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Updated Name", foundUser.get().getNom());
        assertEquals("9876543210", foundUser.get().getTelephone());
    }

    @Test
    @DisplayName("Test unique email constraint")
    void testUniqueEmailConstraint() {
        // Given
        User user1 = new User();
        user1.setNom("User 1");
        user1.setEmail("same@example.com");
        user1.setMotDePasse("password1");
        user1.setRole(Role.CLIENT);

        User user2 = new User();
        user2.setNom("User 2");
        user2.setEmail("same@example.com"); // Same email
        user2.setMotDePasse("password2");
        user2.setRole(Role.CLIENT);

        // When & Then
        entityManager.persistAndFlush(user1);
        assertThrows(Exception.class, () -> {
            entityManager.persistAndFlush(user2);
        });
    }


}

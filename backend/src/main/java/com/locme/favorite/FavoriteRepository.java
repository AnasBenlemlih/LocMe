package com.locme.favorite;

import com.locme.auth.User;
import com.locme.voiture.Voiture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserAndVoiture(User user, Voiture voiture);
    List<Favorite> findByUser(User user);
    List<Favorite> findByVoiture(Voiture voiture);
    boolean existsByUserAndVoiture(User user, Voiture voiture);
    void deleteByUserAndVoiture(User user, Voiture voiture);
}

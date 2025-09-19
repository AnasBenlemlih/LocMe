package com.locme.favorite;

import com.locme.auth.User;
import com.locme.common.exceptions.BusinessException;
import com.locme.common.exceptions.ResourceNotFoundException;
import com.locme.voiture.Voiture;
import com.locme.voiture.VoitureRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private VoitureRepository voitureRepository;

    public Favorite toggleFavorite(Long voitureId, User user) {
        Voiture voiture = voitureRepository.findById(voitureId)
                .orElseThrow(() -> new ResourceNotFoundException("Voiture non trouvée"));

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndVoiture(user, voiture);
        
        if (existingFavorite.isPresent()) {
            // Remove from favorites
            favoriteRepository.delete(existingFavorite.get());
            return null;
        } else {
            // Add to favorites
            Favorite favorite = new Favorite(user, voiture);
            return favoriteRepository.save(favorite);
        }
    }

    public List<Favorite> getUserFavorites(User user) {
        return favoriteRepository.findByUser(user);
    }

    public boolean isFavorite(Long voitureId, User user) {
        Voiture voiture = voitureRepository.findById(voitureId)
                .orElseThrow(() -> new ResourceNotFoundException("Voiture non trouvée"));
        
        return favoriteRepository.existsByUserAndVoiture(user, voiture);
    }

    public void removeFavorite(Long voitureId, User user) {
        Voiture voiture = voitureRepository.findById(voitureId)
                .orElseThrow(() -> new ResourceNotFoundException("Voiture non trouvée"));
        
        favoriteRepository.deleteByUserAndVoiture(user, voiture);
    }
}

package com.locme.favorite;

import com.locme.auth.AuthService;
import com.locme.auth.User;
import com.locme.common.ApiResponse;
import com.locme.common.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private AuthService authService;

    @PostMapping("/toggle/{voitureId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Favorite>> toggleFavorite(@PathVariable Long voitureId) {
        try {
            User currentUser = authService.getCurrentUser();
            Favorite favorite = favoriteService.toggleFavorite(voitureId, currentUser);
            
            if (favorite != null) {
                return ResponseEntity.ok(ApiResponse.success("Ajouté aux favoris", favorite));
            } else {
                return ResponseEntity.ok(ApiResponse.success("Retiré des favoris", null));
            }
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Favori non trouvé"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{id}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Favorite>>> getUserFavorites(@PathVariable Long id) {
        try {
            User currentUser = authService.getCurrentUser();
            
            // Check if user can access these favorites
            if (!currentUser.getId().equals(id) && !currentUser.getRole().name().equals("ADMIN")) {
                return ResponseEntity.status(403).body(ApiResponse.error("Accès non autorisé"));
            }
            
            List<Favorite> favorites = favoriteService.getUserFavorites(currentUser);
            return ResponseEntity.ok(ApiResponse.success(favorites));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/check/{voitureId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(@PathVariable Long voitureId) {
        try {
            User currentUser = authService.getCurrentUser();
            boolean isFavorite = favoriteService.isFavorite(voitureId, currentUser);
            return ResponseEntity.ok(ApiResponse.success(isFavorite));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Favori non trouvé"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{voitureId}")
    @PreAuthorize("hasRole('CLIENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(@PathVariable Long voitureId) {
        try {
            User currentUser = authService.getCurrentUser();
            favoriteService.removeFavorite(voitureId, currentUser);
            return ResponseEntity.ok(ApiResponse.success("Favori supprimé", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(ApiResponse.error("Favori non trouvé"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}

package com.locme.voiture;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/images")
@CrossOrigin(origins = "*")
public class ImageController {

    @GetMapping("/voitures/{imageName}")
    public ResponseEntity<Resource> getVoitureImage(@PathVariable String imageName) {
        try {
            // Essayer d'abord avec l'extension fournie
            Resource resource = new ClassPathResource("static/images/voitures/" + imageName);
            
            // Si l'image n'existe pas, essayer avec .svg
            if (!resource.exists() && !imageName.endsWith(".svg")) {
                resource = new ClassPathResource("static/images/voitures/" + imageName.replaceAll("\\.(jpg|jpeg|png)$", ".svg"));
            }
            
            // Si toujours pas trouvé, utiliser l'image par défaut
            if (!resource.exists()) {
                resource = new ClassPathResource("static/images/voitures/default-car.svg");
            }
            
            String contentType;
            if (imageName.endsWith(".svg") || resource.getFilename().endsWith(".svg")) {
                contentType = "image/svg+xml";
            } else {
                contentType = Files.probeContentType(resource.getFile().toPath());
                if (contentType == null) {
                    contentType = "image/jpeg";
                }
            }
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageName + "\"")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

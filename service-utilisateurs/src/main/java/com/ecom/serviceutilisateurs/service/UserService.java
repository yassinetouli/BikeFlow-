package com.ecom.serviceutilisateurs.service;

import com.ecom.serviceutilisateurs.dto.UserEligibilityDTO;
import com.ecom.serviceutilisateurs.dto.UserRequestDTO;
import com.ecom.serviceutilisateurs.dto.UserResponseDTO;
import com.ecom.serviceutilisateurs.entity.InvoiceStatus;
import com.ecom.serviceutilisateurs.entity.User;
import com.ecom.serviceutilisateurs.messaging.NotificationPublisher;
import com.ecom.serviceutilisateurs.repository.InvoiceRepository;
import com.ecom.serviceutilisateurs.repository.UserRepository;
import com.ecom.serviceutilisateurs.dto.Notif_EmailDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final NotificationPublisher notificationPublisher;

    public UserService(UserRepository userRepository, InvoiceRepository invoiceRepository, NotificationPublisher notificationPublisher) {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
        this.notificationPublisher = notificationPublisher;
    }

    public List<UserResponseDTO> findAll() {
        return userRepository.findAll()
                .stream()
                .map(u -> UserResponseDTO.fromEntity(u, hasImpayes(u.getId())))
                .collect(Collectors.toList());
    }

    public UserResponseDTO findById(Long id) {
        User user = findUserEntity(id);
        return UserResponseDTO.fromEntity(user, hasImpayes(id));
    }

    public UserResponseDTO create(UserRequestDTO dto) {

        // 1. Verify email uniqueness
        verifierEmailUnique(dto.getEmail(), null);

        // 2. Create user
        User user = new User();
        user.setNom(dto.getNom());
        user.setEmail(dto.getEmail());
        user.setMoyenPaiement(dto.getMoyenPaiement());
        user.setSuspendu(dto.getSuspendu() != null
                ? dto.getSuspendu()
                : false);

        // 3. Save user
        User savedUser = userRepository.save(user);

        // 4. Create notification DTO
        Notif_EmailDTO.NotificationEmailDto notification =
                new Notif_EmailDTO.NotificationEmailDto(
                        savedUser.getEmail(),
                        "Bienvenue !",
                        "Bonjour " + savedUser.getNom()
                                + ", votre compte a été créé avec succès."
                );

        // 5. Publish notification through RabbitMQ
        notificationPublisher.envoyer(notification);

        // 6. Return user response
        return UserResponseDTO.fromEntity(savedUser, false);
    }

    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = findUserEntity(id);

        verifierEmailUnique(dto.getEmail(), user.getEmail());

        user.setNom(dto.getNom());
        user.setEmail(dto.getEmail());
        user.setMoyenPaiement(dto.getMoyenPaiement());
        if (dto.getSuspendu() != null) {
            user.setSuspendu(dto.getSuspendu());
        }

        User updatedUser = userRepository.save(user);
        return UserResponseDTO.fromEntity(updatedUser, hasImpayes(id));
    }

    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw introuvable(id);
        }
        userRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    public boolean hasImpayes(Long userId) {
        return invoiceRepository.existsByUserIdAndStatut(userId, InvoiceStatus.IMPAYEE);
    }

    public UserEligibilityDTO checkEligibility(Long userId) {
        User user = findUserEntity(userId);
        boolean impayes = hasImpayes(userId);
        boolean moyenPaiementValide = user.getMoyenPaiement() != null && !user.getMoyenPaiement().isBlank();

        if (user.isSuspendu()) {
            return new UserEligibilityDTO(userId, false, true, moyenPaiementValide, impayes,
                    "Compte suspendu");
        }
        if (impayes) {
            return new UserEligibilityDTO(userId, false, false, moyenPaiementValide, true,
                    "Facture impayee detectee");
        }
        if (!moyenPaiementValide) {
            return new UserEligibilityDTO(userId, false, false, false, false,
                    "Moyen de paiement manquant");
        }

        return new UserEligibilityDTO(userId, true, false, true, false,
                "Utilisateur eligible");
    }

    private User findUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> introuvable(id));
    }

    private void verifierEmailUnique(String email, String emailActuel) {
        boolean emailInchange = email != null && email.equalsIgnoreCase(emailActuel);
        if (!emailInchange && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Email deja utilise");
        }
    }

    private ResponseStatusException introuvable(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Utilisateur introuvable");
    }
}

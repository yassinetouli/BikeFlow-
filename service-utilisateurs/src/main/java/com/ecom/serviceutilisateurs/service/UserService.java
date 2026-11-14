package com.ecom.serviceutilisateurs.service;

import com.ecom.serviceutilisateurs.dto.UserEligibilityDTO;
import com.ecom.serviceutilisateurs.dto.UserRequestDTO;
import com.ecom.serviceutilisateurs.dto.UserResponseDTO;
import com.ecom.serviceutilisateurs.entity.InvoiceStatus;
import com.ecom.serviceutilisateurs.entity.User;
import com.ecom.serviceutilisateurs.repository.InvoiceRepository;
import com.ecom.serviceutilisateurs.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;

    public UserService(UserRepository userRepository, InvoiceRepository invoiceRepository) {
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
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
        verifierEmailUnique(dto.getEmail(), null);

        User user = new User();
        user.setNom(dto.getNom());
        user.setEmail(dto.getEmail());
        user.setMoyenPaiement(dto.getMoyenPaiement());
        user.setSuspendu(dto.getSuspendu() != null ? dto.getSuspendu() : false);

        User savedUser = userRepository.save(user);
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

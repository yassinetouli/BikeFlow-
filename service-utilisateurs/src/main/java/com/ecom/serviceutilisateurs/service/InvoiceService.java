package com.ecom.serviceutilisateurs.service;

import com.ecom.serviceutilisateurs.dto.InvoiceRequestDTO;
import com.ecom.serviceutilisateurs.dto.InvoiceResponseDTO;
import com.ecom.serviceutilisateurs.entity.Invoice;
import com.ecom.serviceutilisateurs.entity.InvoiceStatus;
import com.ecom.serviceutilisateurs.repository.InvoiceRepository;
import com.ecom.serviceutilisateurs.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    public List<InvoiceResponseDTO> findAll() {
        return invoiceRepository.findAll()
                .stream()
                .map(InvoiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public InvoiceResponseDTO findById(Long id) {
        Invoice invoice = findInvoiceEntity(id);
        return InvoiceResponseDTO.fromEntity(invoice);
    }

    public List<InvoiceResponseDTO> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Utilisateur introuvable");
        }
        return invoiceRepository.findByUserId(userId)
                .stream()
                .map(InvoiceResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public InvoiceResponseDTO create(InvoiceRequestDTO dto) {
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur introuvable");
        }

        Invoice invoice = new Invoice();
        invoice.setRentalId(dto.getRentalId());
        invoice.setUserId(dto.getUserId());
        invoice.setMontant(dto.getMontant());
        invoice.setDate(dto.getDate() != null ? dto.getDate() : LocalDate.now());
        invoice.setEcheance(dto.getEcheance() != null ? dto.getEcheance() : LocalDate.now().plusDays(30));
        invoice.setStatut(dto.getStatut() != null ? dto.getStatut() : InvoiceStatus.EN_ATTENTE);

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceResponseDTO.fromEntity(saved);
    }

    public InvoiceResponseDTO updateStatut(Long id, InvoiceStatus nouveauStatut) {
        Invoice invoice = findInvoiceEntity(id);
        invoice.setStatut(nouveauStatut);
        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceResponseDTO.fromEntity(saved);
    }

    public InvoiceResponseDTO update(Long id, InvoiceRequestDTO dto) {
        Invoice invoice = findInvoiceEntity(id);

        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur introuvable");
        }

        invoice.setRentalId(dto.getRentalId());
        invoice.setUserId(dto.getUserId());
        invoice.setMontant(dto.getMontant());
        if (dto.getDate() != null) {
            invoice.setDate(dto.getDate());
        }
        if (dto.getEcheance() != null) {
            invoice.setEcheance(dto.getEcheance());
        }
        if (dto.getStatut() != null) {
            invoice.setStatut(dto.getStatut());
        }

        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceResponseDTO.fromEntity(saved);
    }

    public void delete(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture introuvable");
        }
        invoiceRepository.deleteById(id);
    }

    private Invoice findInvoiceEntity(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Facture introuvable"));
    }
}

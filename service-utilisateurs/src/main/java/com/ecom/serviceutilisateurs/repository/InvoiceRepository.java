package com.ecom.serviceutilisateurs.repository;

import com.ecom.serviceutilisateurs.entity.Invoice;
import com.ecom.serviceutilisateurs.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByUserId(Long userId);

    Optional<Invoice> findByRentalId(Long rentalId);

    boolean existsByUserIdAndStatut(Long userId, InvoiceStatus statut);

    List<Invoice> findByUserIdAndStatut(Long userId, InvoiceStatus statut);
}

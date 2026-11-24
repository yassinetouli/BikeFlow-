package com.ecom.serviceutilisateurs.controller;

import com.ecom.serviceutilisateurs.dto.InvoiceRequestDTO;
import com.ecom.serviceutilisateurs.dto.InvoiceResponseDTO;
import com.ecom.serviceutilisateurs.entity.InvoiceStatus;
import com.ecom.serviceutilisateurs.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceService.findAll();
    }

    @GetMapping("/{id}")
    public InvoiceResponseDTO getInvoiceById(@PathVariable Long id) {
        return invoiceService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvoiceResponseDTO createInvoice(@Valid @RequestBody InvoiceRequestDTO request) {
        return invoiceService.create(request);
    }

    @PutMapping("/{id}")
    public InvoiceResponseDTO updateInvoice(@PathVariable Long id, @Valid @RequestBody InvoiceRequestDTO request) {
        return invoiceService.update(id, request);
    }

    @PutMapping("/{id}/statut")
    public InvoiceResponseDTO updateStatut(@PathVariable Long id, @RequestParam InvoiceStatus statut) {
        return invoiceService.updateStatut(id, statut);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvoice(@PathVariable Long id) {
        invoiceService.delete(id);
    }
}

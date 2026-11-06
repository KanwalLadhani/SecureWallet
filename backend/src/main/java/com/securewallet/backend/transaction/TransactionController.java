package com.securewallet.backend.transaction;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransferService transferService;

    public TransactionController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(Authentication authentication,
                                        @Valid @RequestBody TransferRequest request) {
        return transferService.transfer(authentication.getName(), request);
    }

    @GetMapping
    public List<TransactionResponse> history(Authentication authentication) {
        return transferService.history(authentication.getName());
    }
}
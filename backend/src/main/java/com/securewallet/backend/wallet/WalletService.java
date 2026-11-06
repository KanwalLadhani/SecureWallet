package com.securewallet.backend.wallet;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional(readOnly = true)
    public WalletResponse getMyWallet(String email) {
        Wallet wallet = walletRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Wallet not found"));
        return new WalletResponse(wallet.getBalance());
    }
}
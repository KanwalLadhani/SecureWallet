package com.securewallet.backend.transaction;

import com.securewallet.backend.n8n.FraudCheckClient;
import com.securewallet.backend.wallet.Wallet;
import com.securewallet.backend.wallet.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransferService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final FraudCheckClient fraudCheckClient;
    private final TransactionTemplate transactionTemplate;

    public TransferService(WalletRepository walletRepository,
                           TransactionRepository transactionRepository,
                           FraudCheckClient fraudCheckClient,
                           TransactionTemplate transactionTemplate) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.fraudCheckClient = fraudCheckClient;
        this.transactionTemplate = transactionTemplate;
    }

    // No @Transactional here on purpose: we ask n8n BEFORE touching the database.
    public TransactionResponse transfer(String senderEmail, TransferRequest request) {
        String receiverEmail = request.receiverEmail().trim();
        BigDecimal amount = request.amount();
        String note = (request.note() == null || request.note().isBlank())
                ? null : request.note().trim();

        // 1) Ask the security officer (n8n) for a verdict
        FraudCheckClient.Verdict verdict = fraudCheckClient.check(senderEmail, receiverEmail, amount);
        TransactionStatus status = TransactionStatus.valueOf(verdict.decision());

        // 2) Only now open the database transaction and do the real work
        return transactionTemplate.execute(tx ->
                doTransfer(senderEmail, receiverEmail, amount, note, status));
    }

    private TransactionResponse doTransfer(String senderEmail, String receiverEmail,
                                           BigDecimal amount, String note,
                                           TransactionStatus status) {
        Long senderId = walletRepository.findIdByUserEmail(senderEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "You have no wallet"));
        Long receiverId = walletRepository.findIdByUserEmail(receiverEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Receiver not found"));

        if (senderId.equals(receiverId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You cannot send money to yourself");
        }

        // Lock both wallets (always in id order) and read their fresh balances
        List<Wallet> locked = walletRepository.findAllByIdForUpdate(List.of(senderId, receiverId));
        Wallet sender = locked.stream()
                .filter(w -> w.getId().equals(senderId)).findFirst().orElseThrow();
        Wallet receiver = locked.stream()
                .filter(w -> w.getId().equals(receiverId)).findFirst().orElseThrow();

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        // BLOCKED = red light: the money stays where it is
        if (status != TransactionStatus.BLOCKED) {
            sender.setBalance(sender.getBalance().subtract(amount));
            receiver.setBalance(receiver.getBalance().add(amount));
        }

        Transaction saved = transactionRepository.save(
                new Transaction(sender, receiver, amount, status, note));

        return new TransactionResponse(
                saved.getId(), senderEmail, receiverEmail, amount,
                saved.getStatus(), saved.getNote(), saved.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(String email) {
        Long walletId = walletRepository.findIdByUserEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "You have no wallet"));

        return transactionRepository.findAllForWallet(walletId).stream()
                .map(t -> new TransactionResponse(
                        t.getId(),
                        t.getSenderWallet().getUser().getEmail(),
                        t.getReceiverWallet().getUser().getEmail(),
                        t.getAmount(),
                        t.getStatus(),
                        t.getNote(),
                        t.getCreatedAt()))
                .toList();
    }
}
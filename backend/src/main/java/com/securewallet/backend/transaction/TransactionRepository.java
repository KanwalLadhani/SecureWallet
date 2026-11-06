package com.securewallet.backend.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
            SELECT t FROM Transaction t
            JOIN FETCH t.senderWallet
            JOIN FETCH t.receiverWallet
            WHERE t.senderWallet.id = :walletId OR t.receiverWallet.id = :walletId
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findAllForWallet(@Param("walletId") Long walletId);
}
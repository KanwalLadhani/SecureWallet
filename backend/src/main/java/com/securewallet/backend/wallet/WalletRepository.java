package com.securewallet.backend.wallet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUserId(Long userId);

    Optional<Wallet> findByUserEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select w from Wallet w where w.id in :ids order by w.id")
    List<Wallet> findAllByIdForUpdate(@Param("ids") List<Long> ids);
    @Query("select w.id from Wallet w where w.user.email = :email")
    Optional<Long> findIdByUserEmail(@Param("email") String email);
}
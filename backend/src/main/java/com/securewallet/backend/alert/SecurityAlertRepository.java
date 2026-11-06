package com.securewallet.backend.alert;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SecurityAlertRepository extends JpaRepository<SecurityAlert, Long> {

    // newest first; Pageable lets us limit how many rows we fetch
    List<SecurityAlert> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
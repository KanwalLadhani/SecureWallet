package com.securewallet.backend.alert;

import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/alerts")
public class AdminAlertController {

    private final SecurityAlertRepository repository;

    public AdminAlertController(SecurityAlertRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<AlertResponse> latest() {
        return repository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 100))
                .stream()
                .map(AlertResponse::from)
                .toList();
    }
}
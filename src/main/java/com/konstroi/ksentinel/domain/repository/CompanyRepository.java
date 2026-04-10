package com.konstroi.ksentinel.domain.repository;

import com.konstroi.ksentinel.domain.model.Company;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @EntityGraph(attributePaths = "notificationEmails")
    List<Company> findAllByUserIdOrderByName(Long userId);

    @EntityGraph(attributePaths = "notificationEmails")
    Optional<Company> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNameIgnoreCase(Long userId, String name);

    boolean existsByUserIdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);
}

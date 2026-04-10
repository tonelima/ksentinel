package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.domain.repository.AppUserRepository;
import com.konstroi.ksentinel.domain.repository.ApiConfigRepository;
import com.konstroi.ksentinel.domain.repository.CompanyRepository;
import com.konstroi.ksentinel.exception.CompanyInUseException;
import com.konstroi.ksentinel.exception.CompanyNotFoundException;
import com.konstroi.ksentinel.exception.DuplicateCompanyException;
import com.konstroi.ksentinel.infrastructure.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository repository;
    private final ApiConfigRepository apiConfigRepository;
    private final AppUserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<Company> findAll() {
        return repository.findAllByUserIdOrderByName(currentUserService.currentUserId());
    }

    @Transactional(readOnly = true)
    public Company findById(Long id) {
        return repository.findByIdAndUserId(id, currentUserService.currentUserId())
                .orElseThrow(() -> new CompanyNotFoundException(id));
    }

    @Transactional
    public Company create(Company company) {
        Long userId = currentUserService.currentUserId();
        validateUniqueName(userId, company.getName(), null);
        company.setNotificationEmails(normalizeEmails(company.getNotificationEmails()));
        company.setUser(userRepository.getReferenceById(userId));
        return repository.save(company);
    }

    @Transactional
    public Company update(Long id, Company updated) {
        Long userId = currentUserService.currentUserId();
        Company existing = findById(id);
        validateUniqueName(userId, updated.getName(), id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setNotificationEmails(normalizeEmails(updated.getNotificationEmails()));
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Company company = findById(id);
        if (apiConfigRepository.existsByCompanyIdAndCompanyUserId(id, currentUserService.currentUserId())) {
            throw new CompanyInUseException(id);
        }
        repository.delete(company);
    }

    private void validateUniqueName(Long userId, String name, Long idToIgnore) {
        boolean exists = idToIgnore == null
                ? repository.existsByUserIdAndNameIgnoreCase(userId, name)
                : repository.existsByUserIdAndNameIgnoreCaseAndIdNot(userId, name, idToIgnore);
        if (exists) {
            throw new DuplicateCompanyException(name);
        }
    }

    private Set<String> normalizeEmails(Set<String> emails) {
        if (emails == null) {
            return new LinkedHashSet<>();
        }
        return emails.stream()
                .filter(email -> email != null && !email.isBlank())
                .map(email -> email.trim().toLowerCase(Locale.ROOT))
                .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll);
    }
}

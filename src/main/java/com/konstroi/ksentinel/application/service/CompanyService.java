package com.konstroi.ksentinel.application.service;

import com.konstroi.ksentinel.domain.model.Company;
import com.konstroi.ksentinel.domain.repository.ApiConfigRepository;
import com.konstroi.ksentinel.domain.repository.CompanyRepository;
import com.konstroi.ksentinel.exception.CompanyInUseException;
import com.konstroi.ksentinel.exception.CompanyNotFoundException;
import com.konstroi.ksentinel.exception.DuplicateCompanyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository repository;
    private final ApiConfigRepository apiConfigRepository;

    @Transactional(readOnly = true)
    public List<Company> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Company findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
    }

    @Transactional
    public Company create(Company company) {
        validateUniqueName(company.getName(), null);
        return repository.save(company);
    }

    @Transactional
    public Company update(Long id, Company updated) {
        Company existing = findById(id);
        validateUniqueName(updated.getName(), id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Company company = findById(id);
        if (apiConfigRepository.existsByCompanyId(id)) {
            throw new CompanyInUseException(id);
        }
        repository.delete(company);
    }

    private void validateUniqueName(String name, Long idToIgnore) {
        boolean exists = idToIgnore == null
                ? repository.existsByNameIgnoreCase(name)
                : repository.existsByNameIgnoreCaseAndIdNot(name, idToIgnore);
        if (exists) {
            throw new DuplicateCompanyException(name);
        }
    }
}

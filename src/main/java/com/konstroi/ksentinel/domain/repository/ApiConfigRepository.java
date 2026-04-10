package com.konstroi.ksentinel.domain.repository;

import com.konstroi.ksentinel.domain.model.ApiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApiConfigRepository extends JpaRepository<ApiConfig, Long> {

    List<ApiConfig> findAllByEnabledTrue();

    List<ApiConfig> findAllByCompanyId(Long companyId);

    List<ApiConfig> findAllByCompanyIdAndCompanyUserId(Long companyId, Long userId);

    @Query("SELECT DISTINCT a FROM ApiConfig a " +
            "LEFT JOIN FETCH a.company " +
            "LEFT JOIN FETCH a.credential " +
            "LEFT JOIN FETCH a.validationRules " +
            "WHERE a.id = :id")
    java.util.Optional<ApiConfig> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM ApiConfig a " +
            "LEFT JOIN FETCH a.company c " +
            "LEFT JOIN FETCH a.credential " +
            "LEFT JOIN FETCH a.validationRules " +
            "WHERE a.id = :id AND c.user.id = :userId")
    java.util.Optional<ApiConfig> findByIdWithDetailsAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT a FROM ApiConfig a " +
            "LEFT JOIN FETCH a.company " +
            "LEFT JOIN FETCH a.credential " +
            "LEFT JOIN FETCH a.validationRules " +
            "ORDER BY a.name")
    List<ApiConfig> findAllWithDetails();

    @Query("SELECT DISTINCT a FROM ApiConfig a " +
            "LEFT JOIN FETCH a.company c " +
            "LEFT JOIN FETCH a.credential " +
            "LEFT JOIN FETCH a.validationRules " +
            "WHERE c.user.id = :userId " +
            "ORDER BY a.name")
    List<ApiConfig> findAllWithDetailsByUserId(@Param("userId") Long userId);

    boolean existsByName(String name);

    boolean existsByCompanyId(Long companyId);

    boolean existsByCompanyIdAndCompanyUserId(Long companyId, Long userId);
}

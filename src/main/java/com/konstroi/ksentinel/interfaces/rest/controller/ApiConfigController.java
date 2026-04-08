package com.konstroi.ksentinel.interfaces.rest.controller;

import com.konstroi.ksentinel.application.service.ApiConfigService;
import com.konstroi.ksentinel.domain.model.ApiConfig;
import com.konstroi.ksentinel.interfaces.rest.dto.ApiConfigRequest;
import com.konstroi.ksentinel.interfaces.rest.dto.ApiConfigResponse;
import com.konstroi.ksentinel.interfaces.rest.mapper.ApiConfigMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
public class ApiConfigController {

    private final ApiConfigService service;
    private final ApiConfigMapper mapper;

    @GetMapping
    public List<ApiConfigResponse> findAll(@RequestParam(required = false) Long companyId) {
        return service.findAll(companyId).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ApiConfigResponse findById(@PathVariable Long id) {
        return mapper.toResponse(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiConfigResponse> create(@Valid @RequestBody ApiConfigRequest request) {
        ApiConfig created = service.create(mapper.toEntity(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @PutMapping("/{id}")
    public ApiConfigResponse update(@PathVariable Long id, @Valid @RequestBody ApiConfigRequest request) {
        return mapper.toResponse(service.update(id, mapper.toEntity(request)));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PatchMapping("/{id}/toggle")
    public ApiConfigResponse toggle(@PathVariable Long id) {
        return mapper.toResponse(service.toggleEnabled(id));
    }
}

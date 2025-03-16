package com.sbomfinder.repository;

import com.sbomfinder.model.ExternalReference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalReferenceRepository extends JpaRepository<ExternalReference, Long> {
    List<ExternalReference> findBySbomId(Long sbomId);
    boolean existsByReferenceLocator(String referenceLocator);
}

package com.sbomfinder.repository;

import com.sbomfinder.model.SoftwarePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SoftwarePackageRepository extends JpaRepository<SoftwarePackage, Long> {
    List<SoftwarePackage> findBySbomId(Long sbomId);
    boolean existsByNameAndVersion(String name, String version);

    @Query("SELECT sp FROM SoftwarePackage sp WHERE sp.sbom.id = :sbomId AND LOWER(sp.name) LIKE %:keyword%")
    List<SoftwarePackage> findPackagesBySbomIdAndKeyword(Long sbomId, String keyword);

    default List<SoftwarePackage> findFirmwareBySbomId(Long sbomId) {
        return findPackagesBySbomIdAndKeyword(sbomId, "firmware");
    }

    default List<SoftwarePackage> findOSBySbomId(Long sbomId) {
        return findPackagesBySbomIdAndKeyword(sbomId, "linux");
    }

    default List<SoftwarePackage> findDriversBySbomId(Long sbomId) {
        return findPackagesBySbomIdAndKeyword(sbomId, "driver");
    }
}



package com.sbomfinder.model;

import jakarta.persistence.*;

@Entity
@Table(name = "software_packages")
public class SoftwarePackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sbom_id", nullable = false)
    private Sbom sbom;

    @Column(nullable = false)
    private String name;

    @Column
    private String version;

    @Column
    private String supplier;

    @Column(name = "download_location")
    private String downloadLocation;

    @Column(name = "license_declared")
    private String licenseDeclared;

    @Column(name = "license_concluded")
    private String licenseConcluded;

    @Column(name = "copyright_text")
    private String copyrightText;

    @Column(name = "component_type") // ✅ New Field: OS, Kernel, or Application
    private String componentType;

    // ✅ Default Constructor
    public SoftwarePackage() {}

    // ✅ Constructor with All Fields
    public SoftwarePackage(Sbom sbom, String name, String version, String supplier, String downloadLocation,
                           String licenseDeclared, String licenseConcluded, String copyrightText,
                           String componentType) {
        this.sbom = sbom;
        this.name = name;
        this.version = version;
        this.supplier = supplier;
        this.downloadLocation = downloadLocation;
        this.licenseDeclared = licenseDeclared;
        this.licenseConcluded = licenseConcluded;
        this.copyrightText = copyrightText;
        this.componentType = componentType; // ✅ Store OS, Kernel, or Application
    }

    // ✅ Getters
    public Long getId() { return id; }
    public Sbom getSbom() { return sbom; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getSupplier() { return supplier; }
    public String getDownloadLocation() { return downloadLocation; }
    public String getLicenseDeclared() { return licenseDeclared; }
    public String getLicenseConcluded() { return licenseConcluded; }
    public String getCopyrightText() { return copyrightText; }
    public String getComponentType() { return componentType; }

    // ✅ Setters
    public void setId(Long id) { this.id = id; }
    public void setSbom(Sbom sbom) { this.sbom = sbom; }
    public void setName(String name) { this.name = name; }
    public void setVersion(String version) { this.version = version; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    public void setDownloadLocation(String downloadLocation) { this.downloadLocation = downloadLocation; }
    public void setLicenseDeclared(String licenseDeclared) { this.licenseDeclared = licenseDeclared; }
    public void setLicenseConcluded(String licenseConcluded) { this.licenseConcluded = licenseConcluded; }
    public void setCopyrightText(String copyrightText) { this.copyrightText = copyrightText; }
    public void setComponentType(String componentType) { this.componentType = componentType; } // ✅ New Field
}

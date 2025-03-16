package com.sbomfinder.dto;

public class SoftwarePackageDTO {
    private String name;
    private String version;
    private String supplier;
    private String componentType;

    public SoftwarePackageDTO(String name, String version, String supplier, String componentType) {
        this.name = name;
        this.version = version;
        this.supplier = supplier;
        this.componentType = componentType;
    }

    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getSupplier() { return supplier; }
    public String getComponentType() { return componentType; }
}

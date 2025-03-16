package com.sbomfinder.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sbomfinder.model.Device;
import com.sbomfinder.model.ExternalReference;
import com.sbomfinder.model.Sbom;
import com.sbomfinder.repository.DeviceRepository;
import com.sbomfinder.repository.ExternalReferenceRepository;
import com.sbomfinder.repository.SbomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import com.sbomfinder.dto.DeviceDetailsDTO;
import com.sbomfinder.dto.ExternalReferenceDTO;
import com.sbomfinder.dto.SoftwarePackageDTO;
import java.io.IOException;
import java.time.OffsetDateTime;
import com.sbomfinder.model.SoftwarePackage;
import com.sbomfinder.repository.SoftwarePackageRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.sbomfinder.dto.CompareDevicesRequest;
import com.sbomfinder.dto.DeviceComparisonDTO;
import com.sbomfinder.model.Device;
import com.sbomfinder.model.SoftwarePackage;
import com.sbomfinder.repository.DeviceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/sboms")
public class SbomController {

    @Autowired
    private SoftwarePackageRepository softwarePackageRepository;

    @Autowired
    private SbomRepository sbomRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ObjectMapper objectMapper; // Jackson JSON Mapper

    @Autowired
    private ExternalReferenceRepository externalReferenceRepository;

    @PostMapping("/upload-sbom")
    public ResponseEntity<String> uploadSbom(@RequestParam("sbomFile") MultipartFile sbomFile,
                                             @RequestParam("category") String category) {
        try {
            // Parse SBOM JSON file
            JsonNode jsonNode = objectMapper.readTree(sbomFile.getInputStream());

            // ✅ Extract values from SBOM
            JsonNode digitalFootprintNode = jsonNode.path("digitalFootprint");
            String digitalFootprint;
            if (digitalFootprintNode.isArray()) {
                digitalFootprint = StreamSupport.stream(digitalFootprintNode.spliterator(), false)
                        .map(JsonNode::asText)
                        .collect(Collectors.joining(", ")); // Convert array to comma-separated string
            } else {
                digitalFootprint = digitalFootprintNode.asText("Not Available");
            }

            String deviceName = jsonNode.path("metadata").path("component").path("name").asText("Unknown Device");
            String manufacturer = jsonNode.path("metadata").path("component").path("manufacturer").asText("Unknown Manufacturer");
            String operatingSystem = jsonNode.path("metadata").path("component").path("operatingSystem").asText("Unknown OS");
            String osVersion = jsonNode.path("metadata").path("component").path("version").asText("Unknown Version");
            String kernelVersion = jsonNode.path("metadata").path("component").path("kernel").asText("Unknown Kernel");
            JsonNode toolsNode = jsonNode.path("metadata").path("tools");
            String vendor = "Unknown Vendor";
            String name = "Unknown";

            if (toolsNode.isArray() && toolsNode.size() > 0) {
                vendor = toolsNode.get(0).path("vendor").asText("Unknown Vendor");
                name = toolsNode.get(0).path("name").asText("Unknown");
            }

            // ✅ Parse timestamp correctly
            OffsetDateTime dateTime = OffsetDateTime.parse(jsonNode.path("metadata").path("timestamp").asText());
            LocalDateTime createdTime = dateTime.toLocalDateTime();
                Sbom newSbom = new Sbom(jsonNode.path("bomFormat").asText(), jsonNode.path("specVersion").asText(),
                        jsonNode.path("dataLicense").asText(), jsonNode.path("documentNamespace").asText(),
                        createdTime,
                        vendor,
                        name);
                sbomRepository.save(newSbom);

            System.out.println("saved successfully");
            JsonNode externalRefsNode = jsonNode.path("externalReferences");
            if (externalRefsNode != null && externalRefsNode.isArray()) {
                for (JsonNode refNode : externalRefsNode) {
                    ExternalReference externalReference = new ExternalReference(
                            newSbom,
                            refNode.path("referenceCategory").asText(),
                            refNode.path("referenceType").asText(),
                            refNode.path("referenceLocator").asText()
                    );
                    externalReferenceRepository.save(externalReference);
                }
            }



            // ✅ Check if Device already exists
            Device existingDevice = deviceRepository.findByDeviceNameAndManufacturer(deviceName, manufacturer).orElse(null);
            if (existingDevice == null) {
                existingDevice = new Device(deviceName, manufacturer, category, operatingSystem, osVersion, kernelVersion, digitalFootprint, newSbom);
                deviceRepository.save(existingDevice);
            }

            // ✅ Extract and Save Software Packages (Avoid Duplicates)
            JsonNode packagesNode = jsonNode.path("components");
            if (packagesNode != null && packagesNode.isArray()) {
                for (JsonNode packageNode : packagesNode) {
                    String packageName = packageNode.path("name").asText();
                    String packageVersion = packageNode.path("version").asText(null);
                    String supplier = packageNode.path("publisher").asText(null);
                    String downloadLocation = packageNode.path("downloadLocation").asText(null);
                    String licenseDeclared = packageNode.path("licenses").size() > 0 ? packageNode.path("licenses").get(0).path("license").path("id").asText(null) : null;
                    String copyrightText = packageNode.path("copyrightText").asText(null);
                    String componentType = packageNode.path("type").asText(null); // OS, Kernel, Application

                    // ✅ Check if Software Package exists before inserting
                    if (!softwarePackageRepository.existsByNameAndVersion(packageName, packageVersion)) {
                        SoftwarePackage softwarePackage = new SoftwarePackage(newSbom, packageName, packageVersion, supplier, downloadLocation,
                                licenseDeclared, licenseDeclared, copyrightText, componentType);
                        softwarePackageRepository.save(softwarePackage);
                    }
                }
            }

            // ✅ Extract and Save External References (Avoid Duplicates)
//            JsonNode externalRefsNode = jsonNode.path("externalReferences");
//            if (externalRefsNode != null && externalRefsNode.isArray()) {
//                for (JsonNode refNode : externalRefsNode) {
//                    String referenceCategory = refNode.path("referenceCategory").asText(null);
//                    String referenceType = refNode.path("referenceType").asText(null);
//                    String referenceLocator = refNode.path("referenceLocator").asText(null);
//
//                    if (referenceCategory != null && referenceType != null && referenceLocator != null) {
//                        // ✅ Check if External Reference exists before inserting
//                        if (!externalReferenceRepository.existsByReferenceLocator(referenceLocator)) {
//                            ExternalReference externalReference = new ExternalReference(newSbom, referenceCategory, referenceType, referenceLocator);
//                            externalReferenceRepository.save(externalReference);
//                        }
//                    }
//                }
//            }

            return ResponseEntity.ok("SBOM, Device, Software Packages, and External References stored successfully without duplicates!");

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing SBOM file: " + e.getMessage());
        }
    }

    // DTO for API Response
    public static class DeviceDetailsResponse {
        public String deviceName;
        public String manufacturer;
        public String category;
        public String operatingSystem;
        public Sbom sbomDetails;
        public List<SoftwarePackage> softwarePackages;
        public List<ExternalReference> externalReferences;

        public DeviceDetailsResponse(Device device, Sbom sbom, List<SoftwarePackage> softwarePackages, List<ExternalReference> externalReferences) {
            this.deviceName = device.getDeviceName();
            this.manufacturer = device.getManufacturer();
            this.category = device.getCategory();
            this.operatingSystem = device.getOperatingSystem();
            this.sbomDetails = sbom;
            this.softwarePackages = softwarePackages;
            this.externalReferences = externalReferences;
        }
    }


}


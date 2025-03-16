package com.sbomfinder.controller;

import com.sbomfinder.dto.*;
import com.sbomfinder.model.Device;
import com.sbomfinder.repository.DeviceRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/devices") // ✅ All device endpoints will be under `/api/devices`
public class DeviceController {

    private final DeviceRepository deviceRepository;

    public DeviceController(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    // ✅ Fetch Device Details by ID
    @GetMapping("/{deviceId}/details")
    public ResponseEntity<?> getDeviceDetails(@PathVariable Long deviceId) {
        Optional<Device> optionalDevice = deviceRepository.findById(deviceId);

        if (optionalDevice.isPresent()) {
            Device device = optionalDevice.get();

            // ✅ Convert Software Packages to DTO
            List<SoftwarePackageDTO> softwarePackages = device.getSbom().getSoftwarePackages().stream()
                    .map(pkg -> new SoftwarePackageDTO(pkg.getName(), pkg.getVersion(), pkg.getSupplier(), pkg.getComponentType()))
                    .collect(Collectors.toList());

            // ✅ Convert External References to DTO
            List<ExternalReferenceDTO> externalReferences = device.getSbom().getExternalReferences().stream()
                    .map(ref -> new ExternalReferenceDTO(ref.getReferenceCategory(), ref.getReferenceType(), ref.getReferenceLocator()))
                    .collect(Collectors.toList());

            // ✅ Create DeviceDetailsDTO Response
            DeviceDetailsDTO deviceDetails = new DeviceDetailsDTO(
                    device.getDeviceName(),
                    device.getManufacturer(),
                    device.getCategory(),
                    device.getOperatingSystem(),
                    device.getOsVersion(),
                    device.getKernelVersion(),
                    device.getDigitalFootprint(),
                    softwarePackages,
                    externalReferences
            );

            return ResponseEntity.ok(deviceDetails);
        } else {
            return ResponseEntity.status(404).body("Device not found");
        }
    }

    // ✅ Compare Two Devices
    @GetMapping("/compare")
    public ResponseEntity<?> compareDevices(@RequestParam Long device1Id, @RequestParam Long device2Id) {
        Optional<Device> optionalDevice1 = deviceRepository.findById(device1Id);
        Optional<Device> optionalDevice2 = deviceRepository.findById(device2Id);

        if (optionalDevice1.isEmpty() || optionalDevice2.isEmpty()) {
            return ResponseEntity.status(404).body("One or both devices not found");
        }

        Device device1 = optionalDevice1.get();
        Device device2 = optionalDevice2.get();
            // ✅ Convert Software Packages to DTO
            List<SoftwarePackageDTO> softwarePackages1 = device1.getSbom().getSoftwarePackages().stream()
                    .map(pkg -> new SoftwarePackageDTO(pkg.getName(), pkg.getVersion(), pkg.getSupplier(), pkg.getComponentType()))
                    .collect(Collectors.toList());

            List<SoftwarePackageDTO> softwarePackages2 = device2.getSbom().getSoftwarePackages().stream()
                    .map(pkg -> new SoftwarePackageDTO(pkg.getName(), pkg.getVersion(), pkg.getSupplier(), pkg.getComponentType()))
                    .collect(Collectors.toList());

            // ✅ Convert External References to DTO
            List<ExternalReferenceDTO> externalRefs1 = device1.getSbom().getExternalReferences().stream()
                    .map(ref -> new ExternalReferenceDTO(ref.getReferenceCategory(), ref.getReferenceType(), ref.getReferenceLocator()))
                    .collect(Collectors.toList());

            List<ExternalReferenceDTO> externalRefs2 = device2.getSbom().getExternalReferences().stream()
                    .map(ref -> new ExternalReferenceDTO(ref.getReferenceCategory(), ref.getReferenceType(), ref.getReferenceLocator()))
                    .collect(Collectors.toList());

            // ✅ Create structured device details (ADD MISSING `category`)
            DeviceDetailsDTO device1Details = new DeviceDetailsDTO(
                    device1.getDeviceName(),
                    device1.getManufacturer(),
                    device1.getCategory(),  // ✅ Ensure category is passed
                    device1.getOperatingSystem(),
                    device1.getOsVersion(),
                    device1.getKernelVersion(),
                    device1.getDigitalFootprint(),
                    softwarePackages1,
                    externalRefs1
            );

            DeviceDetailsDTO device2Details = new DeviceDetailsDTO(
                    device2.getDeviceName(),
                    device2.getManufacturer(),
                    device2.getCategory(),  // ✅ Ensure category is passed
                    device2.getOperatingSystem(),
                    device2.getOsVersion(),
                    device2.getKernelVersion(),
                    device2.getDigitalFootprint(),
                    softwarePackages2,
                    externalRefs2
            );

            // ✅ Prepare structured response without common/unique software lists
            DeviceComparisonDTO comparisonDTO = new DeviceComparisonDTO(device1Details, device2Details);

            return ResponseEntity.ok(comparisonDTO);

    }






}

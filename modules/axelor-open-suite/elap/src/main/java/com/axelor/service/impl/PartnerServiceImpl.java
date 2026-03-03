package com.axelor.service.impl;

import com.axelor.apps.base.db.Partner;
import com.axelor.apps.erp.db.*;
import com.axelor.apps.erp.db.repo.AccessCredentialRepository;
import com.axelor.apps.erp.db.repo.AccessRightsRepository;
import com.axelor.broker.producer.AcsUserProducer;
import com.axelor.client.AcsUserClient;
import com.axelor.dto.DeviceConfigDto;
import com.axelor.dto.PartnerChangePermissionDto;
import com.axelor.dto.PartnerDtoRequest;
import com.axelor.inject.Beans;
import com.axelor.mapper.ChangePermissionMapper;
import com.axelor.mapper.PartnerMapper;
import com.axelor.meta.MetaFiles;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.PartnerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
public class PartnerServiceImpl implements PartnerService {
    private final AccessRightsRepository repository;
    private final AccessCredentialRepository credentialRepository;
    private final PartnerMapper mapper;
    private final ChangePermissionMapper  changePermissionMapper;
    private final AcsUserProducer producer;

    @Inject
    public PartnerServiceImpl(AccessRightsRepository repository, AccessCredentialRepository credentialRepository, PartnerMapper mapper, PartnerChangePermissionDto changeDebtStatusDto, ChangePermissionMapper changePermissionMapper, AcsUserProducer producer) {
        this.repository = repository;
        this.credentialRepository = credentialRepository;
        this.mapper = mapper;
        this.changePermissionMapper = changePermissionMapper;
        this.producer = producer;
    }

    @Transactional
    @Override
    public void partnerSaveDevice(ActionResponse response, ActionRequest request) throws Exception {

        Partner partner = request.getContext().asType(Partner.class);
        if (partner.getPicture() != null) {
            Path path = MetaFiles.getPath(partner.getPicture());
            imageToBase64WithLimit(path);
        }
        if (partner.getUuid() == null) {
            partner.setUuid(UUID.randomUUID().toString().replace("-", ""));
        }
        if (partner.getExternalId() == null || partner.getExternalId().isBlank()) {
            partner.setExternalId(partner.getUuid());
        }
        if ((partner.getFirstName() == null || partner.getFirstName().isBlank()) && partner.getName() != null) {
            partner.setFirstName(partner.getName());
        }
        partner.setUpdatedAt(LocalDateTime.now());
        partner.setVersion(partner.getVersion() == null ? 1 : partner.getVersion() + 1);
        boolean isNew = partner.getId() == null;
        if (isNew) {
            Set<Apartment> apartments = partner.getApartment();
            List<AccessCredential> accessCredentials = partner.getAccessCredentials();
            if (accessCredentials == null || accessCredentials.isEmpty()) {
                accessCredentials = new ArrayList<>();
            }
            if (apartments != null && !apartments.isEmpty()) {
                for (Apartment apartment : apartments) {
                    Entrance entrance = apartment.getEntrance();
                    List<Device> devices = entrance.getDevice();
                    for (Device device : devices) {
                        AccessCredential credential = new AccessCredential();
                        credential.setDevice(device);
                        credential.setAccessRights(findAccessRights());
                        AccessCredential save = credentialRepository.save(credential);
                        accessCredentials.add(save);
                    }
                }
            }
            partner.setAccessCredentials(accessCredentials);
            PartnerDtoRequest dto = mapper.toDto(partner);
            producer.sendUserCreate(dto);
            response.setValues(partner);
        } else {
            Set<Apartment> apartments = partner.getApartment();
            List<AccessCredential> accessCredentials = partner.getAccessCredentials();
            if (accessCredentials == null || accessCredentials.isEmpty()) {
                accessCredentials = new ArrayList<>();
            }
            if (apartments != null && !apartments.isEmpty()) {
                for (Apartment apartment : apartments) {
                    Entrance entrance = apartment.getEntrance();
                    List<Device> devices = entrance.getDevice();
                    for (Device device : devices) {
                        AccessCredential credential = new AccessCredential();
                        credential.setDevice(device);
                        credential.setAccessRights(findAccessRights());
                        AccessCredential save = credentialRepository.save(credential);
                        accessCredentials.add(save);
                    }
                }
            }
            PartnerDtoRequest dto = mapper.toDto(partner);
            producer.sendUserUpdate(partner.getUuid(), dto);
            response.setValues(partner);
            response.setNotify("Partner updated successfully");
        }


    }

    private AccessRights findAccessRights() {
        AccessRights byType = repository.findByType("full_access");
        if (byType == null) {
            AccessRights accessRights = new AccessRights();
            accessRights.setPermissionType("full_access");
            accessRights.setPermissionName("Full access");
            return repository.save(accessRights);
        }
        return byType;
    }

    @Transactional
    @Override
    public void changeDebtStatus(ActionResponse response, ActionRequest request) {
        try {
            Apartment apartment = request.getContext().asType(Apartment.class);
            Set<Partner> partners = apartment.getPartner();
            Entrance entrance = apartment.getEntrance();
            List<Device> devices = entrance.getDevice();
            Boolean isDebt = apartment.getDebtStatus();

            if (partners == null || partners.isEmpty()) {
                response.setNotify("No partners, no blocking required");
                return;
            }
            if (devices == null || devices.isEmpty()) {
                response.setNotify("No device, no blocking required");
                return;
            }
            int updatedPartners = 0;
            int totalDevices = 0;
            for (Partner partner : partners) {
                try {
                    int devicesUpdated = updatePartnerDevicePermissions(
                            partner, devices, isDebt);
                    if (devicesUpdated > 0) {
                        updatedPartners++;
                        totalDevices += devicesUpdated;
                    }
                } catch (Exception e) {
                    log.error("Partner {} error send: {}",
                            partner.getName(), e.getMessage(), e);
                }
            }

            String message = String.format(
                    "%d Partner, %d Device updated. status: %s",
                    updatedPartners, totalDevices,
                    isDebt ? "blocked" : "unblocked"
            );

            log.info(message);
            response.setNotify(message);

        } catch (Exception e) {
            log.error(" Error update partner: {}", e.getMessage(), e);
        }
    }

    @Override
    public void syncStatusPartner(ActionRequest request, ActionResponse response) {
            try {
                Partner partner = request.getContext().asType(Partner.class);

                if (partner == null || partner.getUuid() == null) {
                    response.setError("Partner UUID not!");
                    return;
                }

                List<Map<String, Object>> syncStatusList = getSyncStatusData(partner.getUuid());

                if (syncStatusList.isEmpty()) {
                    response.setInfo("Detailed information not found");
                    return;
                }


                String htmlTable = buildSyncStatusHtml(syncStatusList);

                response.setValue("$syncStatusHtml", htmlTable);
                response.setValue("$showSyncStatus", true);

                log.info("Sync status loaded successfully");

            } catch (Exception e) {
                log.error("Error showing sync status: {}", e.getMessage(), e);
                response.setError("Error: " + e.getMessage());
            }
        }

    private String buildSyncStatusHtml(List<Map<String, Object>> syncStatusList) {
        StringBuilder html = new StringBuilder();

        html.append("<div contenteditable='false' style='padding: 20px; background-color: #f8f9fa; border-radius: 8px; pointer-events: none; user-select: text;'>");
        html.append("<table style='width: 100%; border-collapse: collapse; background-color: white;'>");
        html.append("<thead><tr style='background-color: #3498db; color: white;'>");
        html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Device Name</th>");
        html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Permission Name</th>");
        html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Synchronized At</th>");
        html.append("<th style='padding: 12px; text-align: left; border: 1px solid #ddd;'>Created At</th>");
        html.append("</tr></thead><tbody>");

        for (int i = 0; i < syncStatusList.size(); i++) {
            Map<String, Object> item = syncStatusList.get(i);
            String bgColor = i % 2 == 0 ? "#f8f9fa" : "#ffffff";

            html.append("<tr style='background-color: ").append(bgColor).append(";'>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                    .append(item.get("deviceName") != null ? item.get("deviceName") : "-")
                    .append("</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                    .append(item.get("permissionName") != null ? item.get("permissionName") : "-")
                    .append("</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                    .append(formatDateTime(item.get("synchronizedAt"))).append("</td>");
            html.append("<td style='padding: 10px; border: 1px solid #ddd;'>")
                    .append(formatDateTime(item.get("createdAt"))).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody></table>");
        html.append("<p style='margin-top: 10px; color: #7f8c8d; font-size: 12px;'>Total devices: ")
                .append(syncStatusList.size()).append("</p>");
        html.append("</div>");

        return html.toString();
    }

        private String formatDateTime(Object dateTime) {
            if (dateTime == null) return "-";
            if (dateTime instanceof LocalDateTime) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                return ((LocalDateTime) dateTime).format(formatter);
            }
            return dateTime.toString();
        }

    private List<Map<String, Object>> getSyncStatusData(String uuid) throws JsonProcessingException {
        AcsUserClient acsUserClient = Beans.get(AcsUserClient.class);
        String body = acsUserClient.getUserSync(uuid);
        List<Map<String, Object>> syncStatusList = new ArrayList<>();

        if (body != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            JsonNode rootNode = objectMapper.readTree(body);
            JsonNode dataNode = rootNode.path("data");

            if (dataNode.has("devices") && dataNode.path("devices").isArray()) {
                for (JsonNode deviceNode : dataNode.path("devices")) {
                    Map<String, Object> device = new HashMap<>();

                    device.put("deviceName", getJsonValueAsString(deviceNode, "device_name"));
                    device.put("permissionName", getJsonValueAsString(deviceNode, "permission_name"));

                    if (deviceNode.has("synchronized_at") && !deviceNode.path("synchronized_at").isNull()) {
                        device.put("synchronizedAt", LocalDateTime.parse(
                                deviceNode.path("synchronized_at").asText().replace("Z", "")
                        ));
                    }

                    if (deviceNode.has("created_at") && !deviceNode.path("created_at").isNull()) {
                        device.put("createdAt", LocalDateTime.parse(
                                deviceNode.path("created_at").asText().replace("Z", "")
                        ));
                    }

                    syncStatusList.add(device);
                }
            }
        }

        return syncStatusList;
    }

    private String getJsonValueAsString(JsonNode node, String fieldName) {
        if (node.has(fieldName) && !node.path(fieldName).isNull()) {
            return node.path(fieldName).asText();
        }
        return null;
    }

    private int updatePartnerDevicePermissions(Partner partner, List<Device> entranceDevices, boolean block) {
        int updatedCount = 0;
        List<DeviceConfigDto> deviceConfigList = new ArrayList<>();
        for (Device device : entranceDevices) {
            try {
                AccessRights permission = findOrCreateBlockedPermission(
                        partner, device, block);
                DeviceConfigDto deviceConfig = DeviceConfigDto.builder()
                        .deviceId(device.getUuid())
                        .permissionId(permission.getUuid())
                        .build();
                deviceConfigList.add(deviceConfig);
                updatedCount++;

            } catch (Exception e) {
                log.error("Device {} for permission error send: {}",
                        device.getName(), e.getMessage());
            }
        }
        if (!deviceConfigList.isEmpty()) {
            try {
                PartnerChangePermissionDto dto = changePermissionMapper.changeDebtStatusDto(deviceConfigList);
                producer.sendUserChangePermission(partner.getUuid(), dto);
                log.info("{} - update user permission success", partner.getName());
            } catch (Exception e) {
                log.error("Failed to send update for partner: {}, error: {}",
                        partner.getName(), e.getMessage());
                return 0;
            }
        }
        return updatedCount;
    }

    private AccessRights findOrCreateBlockedPermission(Partner partner, Device device, boolean block) {
        if (block) {
            return partner.getAccessCredentials().stream()
                    .filter(ar -> ar.getDevice() != null)
                    .filter(ar -> ar.getDevice().getId().equals(device.getId()))
                    .filter(ar -> "empty_access".equals(ar.getAccessRights().getPermissionType()))
                    .findFirst()
                    .map(AccessCredential::getAccessRights)
                    .orElse(repository.findByType("empty_access"));

        } else {
            return partner.getAccessCredentials().stream()
                    .filter(adp -> adp.getDevice() != null)
                    .filter(adp -> adp.getDevice().getId().equals(device.getId()))
                    .map(AccessCredential::getAccessRights)
                    .filter(accessRights -> "full_access".equals(accessRights.getPermissionType()))
                    .findFirst()
                    .orElse(repository.findByType("full_access"));
        }
    }

    private void imageToBase64WithLimit(Path path) {
        try {

            byte[] originalBytes = Files.readAllBytes(path);

            BufferedImage image =
                    ImageIO.read(new ByteArrayInputStream(originalBytes));

            if (image == null) {
                throw new RuntimeException("image is null");
            }

            float quality = 0.9f;
            byte[] compressed;

            do {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                Thumbnails.of(image)
                        .scale(1.0)
                        .outputFormat("jpg")
                        .outputQuality(quality)
                        .toOutputStream(baos);

                compressed = baos.toByteArray();
                quality -= 0.05f;

            } while (compressed.length > 100 * 1024 && quality > 0.3f);

            if (compressed.length > 100 * 1024) {
                throw new RuntimeException("image is too large");
            }

            Files.write(path, compressed);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

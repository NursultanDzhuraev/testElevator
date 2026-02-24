package com.axelor.mapper;

import com.axelor.apps.erp.db.AccessRights;
import com.axelor.dto.AccessRightDtoRequest;

public class AccessRightsMapper {
    public AccessRightDtoRequest toDto(AccessRights accessRights){
        if (accessRights.getPermissionName()==null || accessRights.getPermissionType()==null){
            throw new IllegalArgumentException("name and type must not be empty");
        }
        return AccessRightDtoRequest.builder()
                .permissionId(accessRights.getUuid())
                .permissionName(accessRights.getPermissionName())
                .permissionType(accessRights.getPermissionType())
                .validFrom(accessRights.getValidForm())
                .validUntil(accessRights.getValidUntil())
                .build();
    }
}

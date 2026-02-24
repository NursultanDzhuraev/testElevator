package com.axelor.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.PartnerService;
import com.google.inject.Inject;

import java.util.*;


public class PartnerController {

    private final PartnerService partnerService;

    @Inject
    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    public void changeDebtStatus(ActionRequest request, ActionResponse response) {
        partnerService.changeDebtStatus(response, request);
    }

    public void partnerSaveDevice(ActionRequest request, ActionResponse response) throws Exception {
        partnerService.partnerSaveDevice(response, request);
    }

    public void showSyncStatus(ActionRequest request, ActionResponse response) throws Exception {
        partnerService.syncStatusPartner(request, response);
    }
}

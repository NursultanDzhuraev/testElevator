package com.axelor.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.EntranceService;
import com.google.inject.Inject;

public class EntranceController {
    private final EntranceService entranceService;

    @Inject
    public EntranceController(EntranceService entranceService) {
        this.entranceService = entranceService;
    }

    public void numberOfDebtors(ActionRequest request, ActionResponse response) {
        entranceService.numberOfDebtors(request, response);
    }
}

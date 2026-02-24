package com.axelor.web;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.AccessRightsService;
import com.google.inject.Inject;

public class AccessRightsController {
    private final AccessRightsService accessRightsService;

    @Inject
    public AccessRightsController(AccessRightsService accessRightsService) {
        this.accessRightsService = accessRightsService;
    }

    public void accessRightsSave(ActionRequest request, ActionResponse response) {
        accessRightsService.accessRightsSaveAndUpdate(request, response);

    }

    public void accessDelete(ActionRequest request, ActionResponse response){
        accessRightsService.accessDelete(request,response);
    }

    public void accessListDelete(ActionRequest request, ActionResponse response){
        accessRightsService.accessListDelete(request,response);
    }
}

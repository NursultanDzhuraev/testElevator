package com.axelor.service;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public interface AccessRightsService {
    void accessRightsSaveAndUpdate(ActionRequest request, ActionResponse response);

    void accessDelete(ActionRequest request, ActionResponse response);

    void accessListDelete(ActionRequest request, ActionResponse response);
}

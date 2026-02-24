package com.axelor.service;

import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface PartnerService {
    void partnerSaveDevice(ActionResponse response, ActionRequest request) throws Exception;

  void changeDebtStatus(ActionResponse response, ActionRequest request);

    void syncStatusPartner(ActionRequest request, ActionResponse response) throws JsonProcessingException;
}

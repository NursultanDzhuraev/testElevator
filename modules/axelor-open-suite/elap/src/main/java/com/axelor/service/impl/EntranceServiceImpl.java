package com.axelor.service.impl;

import com.axelor.apps.erp.db.Entrance;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.service.EntranceService;

import java.util.Objects;

public class EntranceServiceImpl implements EntranceService {

    @Override
    public void numberOfDebtors(ActionRequest request, ActionResponse response) {
        Entrance entrance = request.getContext().asType(Entrance.class);
        if (entrance == null || entrance.getApartment() == null) {
            return;
        }
        BuildingServiceImpl.Stats stats = entrance.getApartment().
                stream()
                .filter(Objects::nonNull)
                .collect(BuildingServiceImpl.Stats::new,
                        BuildingServiceImpl.Stats::accept,
                        BuildingServiceImpl.Stats::combine);


        response.setValue("numberOfDebtors", stats.numberOfDebtors);
    }
}

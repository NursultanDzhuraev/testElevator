package com.axelor.service.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.axelor.apps.erp.db.Apartment;
import com.axelor.apps.erp.db.Entrance;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EntranceServiceImplTest {

  private final EntranceServiceImpl service = new EntranceServiceImpl();

  @Test
  void shouldSetNumberOfDebtorsFromApartments() {
    Apartment debtor = new Apartment();
    debtor.setDebtStatus(true);
    Apartment nonDebtor = new Apartment();
    nonDebtor.setDebtStatus(false);

    Entrance entrance = new Entrance();
    entrance.setApartment(List.of(debtor, nonDebtor));

    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    ActionResponse response = mock(ActionResponse.class);
    when(request.getContext().asType(Entrance.class)).thenReturn(entrance);

    service.numberOfDebtors(request, response);

    verify(response).setValue("numberOfDebtors", 1);
  }

  @Test
  void shouldNotTouchResponseWhenEntranceIsNull() {
    ActionRequest request = Mockito.mock(ActionRequest.class, Mockito.RETURNS_DEEP_STUBS);
    ActionResponse response = mock(ActionResponse.class);
    when(request.getContext().asType(Entrance.class)).thenReturn(null);

    service.numberOfDebtors(request, response);

    verifyNoInteractions(response);
  }
}

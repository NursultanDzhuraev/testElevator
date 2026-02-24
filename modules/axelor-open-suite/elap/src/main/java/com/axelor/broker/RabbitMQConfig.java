package com.axelor.broker;

import com.axelor.app.AppSettings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitMQConfig {

  private static final AppSettings settings = AppSettings.get();

  public static final String USERNAME = settings.get("axelor.broker.rabbitmq.username");
  public static final String PASSWORD = settings.get("axelor.broker.rabbitmq.password");
  public static final String URL = settings.get("axelor.broker.rabbitmq.url", "127.0.0.1");
  public static final int PORT = settings.getInt("axelor.broker.rabbitmq.port", 5672);
  public static final String VIRTUAL_HOST = settings.get("axelor.broker.rabbitmq.virtualHost", "/");


  public static final String ACS_EXCHANGE = "acs.exchange";
  public static final String DEAD_LETTER_EXCHANGE = "acs.dlx.exchange";

  public static final String ACS_USER_QUEUE = "acs.user.queue";
  public static final String ACS_LOCATION_QUEUE = "acs.location.queue";
  public static final String ACS_DEVICE_QUEUE = "acs.device.queue";
  public static final String ACS_ACCESS_QUEUE = "acs.access.queue";

  public static final String ACS_USER_CREATED = "acs.user.created";
  public static final String ACS_USER_UPDATED = "acs.user.updated";
  public static final String ACS_USER_PERMISSION = "acs.user.permission";
  public static final String ACS_USER_DELETED = "acs.user.deleted";

  public static final String ACS_LOCATION_CREATED = "acs.location.created";
  public static final String ACS_LOCATION_UPDATED = "acs.location.updated";
  public static final String ACS_LOCATION_DELETED = "acs.location.deleted";

  public static final String ACS_DEVICE_CREATED = "acs.device.created";
  public static final String ACS_DEVICE_UPDATED = "acs.device.updated";
  public static final String ACS_DEVICE_DELETED = "acs.device.deleted";

  public static final String ACS_ACCESS_CREATED = "acs.access.created";
  public static final String ACS_ACCESS_UPDATED = "acs.access.updated";
  public static final String ACS_ACCESS_DELETED = "acs.access.deleted";
}

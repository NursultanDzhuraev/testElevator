package com.axelor.broker;

import com.google.inject.Singleton;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class RabbitMQConnectionManager {

  private Connection connection;
  private static final int MAX_RETRY = 3;
  private static final long RETRY_DELAY_MS = 2000;

  public synchronized Connection getConnection() throws Exception {
    if (connection == null || !connection.isOpen()) {
      connection = createConnection();
    }
    return connection;
  }

  public synchronized Channel createChannel() throws Exception {
    Connection conn = getConnection();
    Channel channel = conn.createChannel();
    channel.confirmSelect();
    return channel;
  }

  private Connection createConnection() throws Exception {
    Exception lastException = null;

    for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
      try {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(RabbitMQConfig.URL);
        factory.setPort(RabbitMQConfig.PORT);
        factory.setUsername(RabbitMQConfig.USERNAME);
        factory.setPassword(RabbitMQConfig.PASSWORD);
        factory.setVirtualHost(RabbitMQConfig.VIRTUAL_HOST);

        factory.setAutomaticRecoveryEnabled(true);
        factory.setNetworkRecoveryInterval(5000);
        factory.setRequestedHeartbeat(30);
        factory.setConnectionTimeout(5000);
        factory.setHandshakeTimeout(5000);

        Connection conn = factory.newConnection();

        log.info("RabbitMQ connection established successfully!");
        return conn;

      } catch (Exception e) {
        lastException = e;
        log.error("Connection attempt {} failed: {}", attempt, e.getMessage());

        if (attempt < MAX_RETRY) {
          Thread.sleep(RETRY_DELAY_MS);
        }
      }
    }

    log.error("Failed to connect to RabbitMQ after {} attempts", MAX_RETRY);
    throw new Exception("RabbitMQ connection failed: " + lastException.getMessage(), lastException);
  }

  public synchronized void close() {
    if (connection != null && connection.isOpen()) {
      try {
        connection.close();
      } catch (Exception e) {
        log.error("Error closing connection: {}", e.getMessage());
      }
    }
  }
}

package com.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.enums.RequestStatuses;
import com.sample.model.BankRequest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.jupiter.api.*;

import javax.jms.*;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceControllerTest {

    private static BrokerService brokerService;
    private ServiceController controller;
    private BankRequest bankRequest;
    private String bankRequestJSON;
    private static Connection connection;
    private static MessageProducer publisher;
    private Message message;

    @BeforeAll
    static void configure() throws Exception {
        brokerService = new BrokerService();
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        brokerService.getManagementContext().setCreateConnector(false);
        brokerService.setAdvisorySupport(false);
        brokerService.setSchedulerSupport(false);
        TransportConnector connector = brokerService.addConnector("tcp://localhost:61616");
        brokerService.start();

        String connectionUri = connector.getPublishableConnectString();
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(connectionUri);
        connection = factory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue("responseQueue");
        publisher = session.createProducer(queue);
        connection.start();
    }

    @AfterAll
    static void stop() throws Exception {
        connection.stop();
        connection = null;
        brokerService.stop();
        brokerService = null;
    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        controller = new ServiceController();
        bankRequest = new BankRequest(1, "Свалов Дмитрий Андреевич", "кредит", new Date(0),
                new Date(0), RequestStatuses.NEW, "Комментарий");
        bankRequestJSON = new ObjectMapper().writeValueAsString(bankRequest);
        message = new ActiveMQTextMessage();
    }

    @AfterEach
    void tearDown() {
        controller = null;
        bankRequest = null;
        message = null;
        bankRequestJSON = null;
    }

    @Test
    void getJSONResponse() throws JMSException, JsonProcessingException {
        message.setStringProperty("status", "ok");
        message.setStringProperty("response", "RESPONSE");

        String response = controller.getJSONResponse("create", message);

        assertEquals("{ \"Response\": \"RESPONSE\" }", response);
    }

    @Test
    void getJSONResponse_with_null_message() throws JsonProcessingException, JMSException {
        String response = controller.getJSONResponse("create", null);
        assertEquals("{ \"Error\": \"Failure to receive a response from the banking system.\" }", response);
    }

    @Test
    void receiveMessage() throws JMSException {
        message.setStringProperty("status", "ok");
        message.setStringProperty("response", bankRequestJSON);
        publisher.send(message);

        Message receivedMessage = controller.receiveMessage();

        assertEquals("ok", receivedMessage.getStringProperty("status"));
        assertEquals(bankRequestJSON, receivedMessage.getStringProperty("response"));
    }
}
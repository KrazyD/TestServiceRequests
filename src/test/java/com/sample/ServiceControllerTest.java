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

    private ServiceController controller;
    private BankRequest bankRequest;
    private String bankRequestJSON;
    private Message message;

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

}
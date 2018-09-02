package com.sample;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sample.enums.LoggerTypes;
import com.sample.model.BankRequest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.jms.*;
import java.util.List;

@RestController
@RequestMapping("/bankRequest")
public class ServiceController {

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String createBankRequest(@RequestBody String request)
            throws JMSException, InterruptedException, JsonProcessingException {

        LoggerWriter.createMessage(LoggerTypes.INFO, "Creating bank request");

        Message message = new ActiveMQTextMessage();
        message.setStringProperty("bankRequest", request);
        message.setStringProperty("action", "create");

        LoggerWriter.createMessage(LoggerTypes.INFO, "Sending a request to the banking system");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = receiveMessage();

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a response from the banking system");

        return getJSONResponse("create", receivedMessage);
    }

    @RequestMapping(method = RequestMethod.PATCH,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String editBankRequest(@RequestBody String request)
            throws JMSException, InterruptedException, JsonProcessingException {

        LoggerWriter.createMessage(LoggerTypes.INFO, "Editing the status of a bank request");

        Message message = new ActiveMQTextMessage();
        message.setObjectProperty("bankRequest", request);
        message.setStringProperty("action", "edit");

        LoggerWriter.createMessage(LoggerTypes.INFO, "Sending a request to the banking system");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = receiveMessage();

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a response from the banking system");

        return getJSONResponse("edit", receivedMessage);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String withdrawnBankRequest(@RequestBody String request)
            throws JMSException, InterruptedException, JsonProcessingException {

        LoggerWriter.createMessage(LoggerTypes.INFO, "Withdrawal of a bank request");

        Message message = new ActiveMQTextMessage();
        message.setObjectProperty("bankRequest", request);
        message.setStringProperty("action", "withdrawn");

        LoggerWriter.createMessage(LoggerTypes.INFO, "Sending a request to the banking system");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = receiveMessage();

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a response from the banking system");

        return getJSONResponse("withdrawn", receivedMessage);
    }

    @RequestMapping(value = "/filter",method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String filterBankRequest(@RequestBody String params)
            throws JMSException, InterruptedException, JsonProcessingException {

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a filtered list of bank applications");

        Message message = new ActiveMQTextMessage();
        message.setStringProperty("params", params);
        message.setStringProperty("action", "filter");

        LoggerWriter.createMessage(LoggerTypes.INFO, "Sending a request to the banking system");
        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = receiveMessage();

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a response from the banking system");

        return getJSONResponse("filter", receivedMessage);
    }

    public String getJSONResponse(String type, Message message) throws JMSException, JsonProcessingException {

        if (message == null) {
            return "{ \"Error\": \"Failure to receive a response from the banking system.\" }";
        }

        if (message.getStringProperty("status").equals("ok")) {

            if (type.equals("filter")) {
                List<BankRequest> filteredRequests = (List<BankRequest>) message.getObjectProperty("response");
                String filteredRequestsJSON = new ObjectMapper().writeValueAsString(filteredRequests);

                return "{ \"Response\": \"" + filteredRequestsJSON + "\" }";
            }
            return "{ \"Response\": \"" + message.getStringProperty("response") + "\" }";

        } else {
            String errorMessage = message.getStringProperty("errorMessage");
            return "{ \"Error\": \"" + errorMessage + "\" }";
        }
    }

    public Message receiveMessage() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        QueueConnection connection = connectionFactory.createQueueConnection();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue sessionQueue = session.createQueue("responseQueue");
        MessageConsumer consumer = session.createConsumer(sessionQueue);
        connection.start();

        return consumer.receive(5000);
    }
}

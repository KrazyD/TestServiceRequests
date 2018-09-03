package com.sample;

import com.sample.enums.LoggerTypes;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.jms.JMSException;
import javax.jms.Message;

@RestController
@RequestMapping("/bankRequest")
public class ServiceController {

    @RequestMapping(method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String createBankRequest(@RequestBody String request)
            throws JMSException, InterruptedException {

        LoggerWriter.createMessage(LoggerTypes.INFO, "Creating bank request");

        Message message = new ActiveMQTextMessage();
        message.setStringProperty("bankRequest", request);
        message.setStringProperty("action", "create");

        LoggerWriter.createMessage(LoggerTypes.INFO, "Sending a request to the banking system");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = Initializer.receiveMessage();

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a response from the banking system");

        return getJSONResponse("create", receivedMessage);
    }

    @RequestMapping(method = RequestMethod.PATCH,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String editBankRequest(@RequestBody String request)
            throws JMSException, InterruptedException {

        LoggerWriter.createMessage(LoggerTypes.INFO, "Editing the status of a bank request");

        Message message = new ActiveMQTextMessage();
        message.setObjectProperty("bankRequest", request);
        message.setStringProperty("action", "edit");

        LoggerWriter.createMessage(LoggerTypes.INFO, "Sending a request to the banking system");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = Initializer.receiveMessage();

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a response from the banking system");

        return getJSONResponse("edit", receivedMessage);
    }

    @RequestMapping(method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String withdrawnBankRequest(@RequestBody String request)
            throws JMSException, InterruptedException {

        LoggerWriter.createMessage(LoggerTypes.INFO, "Withdrawal of a bank request");

        Message message = new ActiveMQTextMessage();
        message.setObjectProperty("bankRequest", request);
        message.setStringProperty("action", "withdrawn");

        LoggerWriter.createMessage(LoggerTypes.INFO, "Sending a request to the banking system");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = Initializer.receiveMessage();

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a response from the banking system");

        return getJSONResponse("withdrawn", receivedMessage);
    }

    @RequestMapping(value = "/filter",method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody String filterBankRequest(@RequestBody String params)
            throws JMSException, InterruptedException {

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a filtered list of bank applications");

        Message message = new ActiveMQTextMessage();
        message.setStringProperty("params", params);
        message.setStringProperty("action", "filter");

        LoggerWriter.createMessage(LoggerTypes.INFO, "Sending a request to the banking system");
        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = Initializer.receiveMessage();

        LoggerWriter.createMessage(LoggerTypes.INFO, "Receiving a response from the banking system");

        return getJSONResponse("filter", receivedMessage);
    }

    public String getJSONResponse(String type, Message message) throws JMSException {

        if (message == null) {
            return "{ \"Error\": \"Failure to receive a response from the banking system.\" }";
        }

        if (message.getStringProperty("status").equals("ok")) {

            if (type.equals("filter")) {
                String filteredRequestsJSON = message.getStringProperty("response");

                return "{ \"Response\": \"" + filteredRequestsJSON + "\" }";
            }
            return "{ \"Response\": \"" + message.getStringProperty("response") + "\" }";

        } else {
            String errorMessage = message.getStringProperty("errorMessage");
            return "{ \"Error\": \"" + errorMessage + "\" }";
        }
    }
}

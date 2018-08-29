package com.sample;

import com.sample.enums.RequestStatuses;
import com.sample.model.BankRequest;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.JMSException;
import javax.jms.Message;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/bankRequest")
public class ServiceController {

    @RequestMapping(method = RequestMethod.PUT)
    public String createBankRequest(@RequestBody BankRequest request) throws InterruptedException, JMSException {

        Message message = new ActiveMQTextMessage();
        message.setObjectProperty("bankRequest", request);
        message.setStringProperty("action", "create");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = Initializer.receiveMessage();

        if (receivedMessage.getStringProperty("status").equals("ok")) {
            return receivedMessage.getObjectProperty("response").toString();
        } else {
            return receivedMessage.getStringProperty("errorMessage");
        }
    }

    @RequestMapping(method = RequestMethod.PATCH)
    public String editBankRequest(@RequestBody BankRequest request) throws JMSException, InterruptedException {

        Message message = new ActiveMQTextMessage();
        message.setObjectProperty("bankRequest", request);
        message.setStringProperty("action", "edit");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = Initializer.receiveMessage();

        if (receivedMessage.getStringProperty("status").equals("ok")) {
            return receivedMessage.getObjectProperty("response").toString();
        } else {
            return receivedMessage.getStringProperty("errorMessage");
        }
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public String withdrawnBankRequest(@RequestBody BankRequest request) throws JMSException, InterruptedException {
        Message message = new ActiveMQTextMessage();
        message.setObjectProperty("bankRequest", request);
        message.setStringProperty("action", "withdrawn");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = Initializer.receiveMessage();

        if (receivedMessage.getStringProperty("status").equals("ok")) {
            return receivedMessage.getObjectProperty("response").toString();
        } else {
            return receivedMessage.getStringProperty("errorMessage");
        }
    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public String filterBankRequest(@RequestBody HashMap<String, String> params) throws JMSException, InterruptedException {
        Message message = new ActiveMQTextMessage();
        message.setObjectProperty("params", params);
        message.setStringProperty("action", "filter");

        Initializer.getBlockingQueue().put(message);
        Message receivedMessage = Initializer.receiveMessage();

        if (receivedMessage.getStringProperty("status").equals("ok")) {
            return receivedMessage.getObjectProperty("filteredRequests").toString();
        } else {
            return receivedMessage.getStringProperty("errorMessage");
        }
    }
}

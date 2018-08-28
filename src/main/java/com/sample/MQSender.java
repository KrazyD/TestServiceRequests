package com.sample;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import javax.jms.*;

public class MQSender implements Runnable {

    private BrokerService brokerService;

    @Override
    public void run() {
        try {

            brokerService = new BrokerService();
            brokerService.setPersistent(false);
            brokerService.setUseJmx(false);
            brokerService.getManagementContext().setCreateConnector(false);
            brokerService.setAdvisorySupport(false);
            brokerService.setSchedulerSupport(false);
            TransportConnector connector = brokerService.addConnector("tcp://localhost:61616");
            brokerService.start();

            String connectionUri = connector.getPublishableConnectString();
            System.out.println(connectionUri);
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(connectionUri);
            Connection connection = factory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("clientQueue");
            MessageProducer publisher = session.createProducer(queue);
            connection.start();

            Message message;

            while (true) {
                message = session.createTextMessage("Text Message");
                message.setStringProperty("name", "Dmitriy");
                publisher.send(message);
                System.out.println("Message successfully sent.");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                brokerService.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

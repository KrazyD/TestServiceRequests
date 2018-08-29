package com.sample;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import javax.jms.*;
import java.util.concurrent.BlockingQueue;

public class MQSender implements Runnable {

    private BlockingQueue<Message> blockingQueue;
    private BrokerService brokerService;
    private TransportConnector connector;

    MQSender(BlockingQueue<Message> queue) throws Exception {
        this.blockingQueue = queue;

        brokerService = new BrokerService();
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        brokerService.getManagementContext().setCreateConnector(false);
        brokerService.setAdvisorySupport(false);
        brokerService.setSchedulerSupport(false);
        connector = brokerService.addConnector("tcp://localhost:61616");
        brokerService.start();
    }

    @Override
    public void run() {
        try {

            String connectionUri = connector.getPublishableConnectString();
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(connectionUri);
            Connection connection = factory.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue queue = session.createQueue("requestQueue");
            MessageProducer publisher = session.createProducer(queue);
            connection.start();

            Message message;

            while (true) {
                if (blockingQueue.size() > 0) {
                    message = blockingQueue.take();
                    publisher.send(message);
                }

                try {
                    Thread.sleep(100);
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

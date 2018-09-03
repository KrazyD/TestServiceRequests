package com.sample;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import javax.jms.*;
import java.util.concurrent.BlockingQueue;

public class MQSender implements Runnable {

    private BlockingQueue<Message> blockingQueue;
    private final String CONNECTION_URI = "tcp://localhost:61616";

    MQSender(BlockingQueue<Message> queue) throws Exception {
        this.blockingQueue = queue;
    }

    @Override
    public void run() {
        try {

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(CONNECTION_URI);
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
        }
    }
}

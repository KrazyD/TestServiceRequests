package com.sample;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.jms.*;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@EnableWebMvc
public class Initializer implements WebApplicationInitializer {
    private static final String DISPATCHER_SERVLET_NAME = "dispatcher";
    private static BlockingQueue<Message> blockingQueue;

    public static BlockingQueue<Message> getBlockingQueue() {
        return blockingQueue;
    }

    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(WebConfig.class);
        servletContext.addListener(new ContextLoaderListener(ctx));
        ctx.setServletContext(servletContext);

        ServletRegistration.Dynamic servlet =
                servletContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(ctx));
        servlet.addMapping("/");
        servlet.setLoadOnStartup(1);

        blockingQueue = new LinkedBlockingDeque<>();

        MQSender sender = null;
        try {
            sender = new MQSender(blockingQueue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(sender);
        thread.setDaemon(true);
        thread.start();
    }

    public static Message receiveMessage() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        QueueConnection connection = connectionFactory.createQueueConnection();
        QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue sessionQueue = session.createQueue("responseQueue");
        MessageConsumer consumer = session.createConsumer(sessionQueue);
        connection.start();

        return consumer.receive(1000);
    }

}

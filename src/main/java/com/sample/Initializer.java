package com.sample;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.PropertyConfigurator;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
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
    private static BlockingQueue<Message> blockingQueue = new LinkedBlockingDeque<>();
    private static MessageConsumer consumer;

    public static BlockingQueue<Message> getBlockingQueue() {
        return blockingQueue;
    }

    @Override
    public void onStartup(ServletContext servletContext) {

        PropertyConfigurator.configure("log4j.properties");

        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("utf-8");
        filter.setForceEncoding(true);
        servletContext.addFilter("/", filter);

        AnnotationConfigWebApplicationContext ctx = new AnnotationConfigWebApplicationContext();
        ctx.register(WebConfig.class);
        servletContext.addListener(new ContextLoaderListener(ctx));
        ctx.setServletContext(servletContext);

        ServletRegistration.Dynamic servlet =
                servletContext.addServlet(DISPATCHER_SERVLET_NAME, new DispatcherServlet(ctx));
        servlet.addMapping("/");
        servlet.setLoadOnStartup(1);

        BrokerService brokerService = new BrokerService();
        brokerService.setPersistent(false);
        brokerService.setUseJmx(false);
        brokerService.getManagementContext().setCreateConnector(false);
        brokerService.setAdvisorySupport(false);
        brokerService.setSchedulerSupport(false);
        try {
            brokerService.addConnector("tcp://localhost:61616");
            brokerService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
            QueueConnection connection = connectionFactory.createQueueConnection();
            QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            Queue sessionQueue = session.createQueue("responseQueue");
            consumer = session.createConsumer(sessionQueue);
            connection.start();

        } catch (JMSException e) {
            e.printStackTrace();
        }

        MQSender sender = null;
        try {
            sender = new MQSender(blockingQueue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(sender);
        thread.start();
    }

    public static Message receiveMessage() throws JMSException {

        return consumer.receive(10000);
    }
}

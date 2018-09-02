package com.sample;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.jms.Message;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@EnableWebMvc
public class Initializer implements WebApplicationInitializer {
    private static final String DISPATCHER_SERVLET_NAME = "dispatcher";
    private static BlockingQueue<Message> blockingQueue = new LinkedBlockingDeque<>();

    public static BlockingQueue<Message> getBlockingQueue() {
        return blockingQueue;
    }

    @Override
    public void onStartup(ServletContext servletContext) {

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

        MQSender sender = null;
        try {
            sender = new MQSender(blockingQueue);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(sender);
        thread.start();
    }
}

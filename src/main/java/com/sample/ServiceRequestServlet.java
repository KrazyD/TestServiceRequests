package com.sample;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(
        name = "servicerequestservlet",
        urlPatterns = "/ServiceRequest"
)
public class ServiceRequestServlet extends HttpServlet {

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {

    }
}

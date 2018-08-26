package com.sample;

import com.sample.model.BankRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceController {

    @RequestMapping("/greeting")
    public BankRequest request() {
        return new BankRequest(1, "Вася");
    }
}

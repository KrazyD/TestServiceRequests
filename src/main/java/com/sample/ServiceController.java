package com.sample;

import com.sample.model.BankRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bankRequest")
public class ServiceController {

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public BankRequest request() {
        return new BankRequest(1, "Вася");
    }
}

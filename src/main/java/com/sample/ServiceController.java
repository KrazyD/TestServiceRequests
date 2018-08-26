package com.sample;

import com.sample.model.BankRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bankRequest")
public class ServiceController {

    @RequestMapping(method = RequestMethod.PUT)
    public BankRequest createBankRequest(@RequestBody BankRequest request) {
        return new BankRequest(1, "Вася");
    }

    @RequestMapping(method = RequestMethod.PATCH)
    public BankRequest editBankRequest(@RequestBody BankRequest request) {
        return new BankRequest(1, "Вася");
    }

    @RequestMapping(method = RequestMethod.DELETE)
    public BankRequest withdrawBankRequest(@RequestBody BankRequest request) {
        return new BankRequest(1, "Вася");
    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public BankRequest filterBankRequest(@RequestBody Object object) {
        return new BankRequest(1, "Вася");
    }
}

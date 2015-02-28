package com.futilemind.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Placeholder controller that says hello for /
 */

@Controller
public class TestController {

    @RequestMapping("/")
    public String getMessage(){
        return "hello";
    }


}

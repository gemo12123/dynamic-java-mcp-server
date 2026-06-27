package org.mytest.test.controller;

import org.mytest.test.annotation.Tool;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gemo
 * @date 2026/6/27 16:35
 */
@RestController
public class ExampleController {

    @GetMapping("/hello-world")
    @Tool(name = "helloWorld")
    public String helloWorld(){
        return "hello world!";
    }
}

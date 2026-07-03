package org.mytest.test.controller;

import org.mytest.test.annotation.Tool;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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

    @PostMapping("/hello-world-post")
    @Tool(name = "hello-world-post")
    public String post(@RequestBody Map<String, Object> map){
        return String.valueOf(map);
    }
}

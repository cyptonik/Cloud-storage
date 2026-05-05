package org.cloud.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@SpringBootApplication
@Controller
public class Application {
	static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}

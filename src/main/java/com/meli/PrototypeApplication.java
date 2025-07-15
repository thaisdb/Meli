package com.meli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.HiddenHttpMethodFilter;


/**
 * Hello world!
 *
 */
@SpringBootApplication
public class PrototypeApplication 
{
    public static void main(String[] args) {
        System.out.println("Application Working Directory: " + System.getProperty("user.dir"));
        SpringApplication.run(PrototypeApplication.class, args);
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}


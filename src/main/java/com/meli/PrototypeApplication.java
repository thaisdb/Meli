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
        SpringApplication.run(PrototypeApplication.class, args);
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
}

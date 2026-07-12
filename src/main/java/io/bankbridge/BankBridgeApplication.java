package io.bankbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BankBridgeApplication {
    public static void main(String[] args) {
        SpringApplication.run(BankBridgeApplication.class, args);
    }
}

package com.javadevelopersguide.springboot.example;
import org.springframework.boot.SpringApplication;
/**
 * This Luncher for the spring boot application.
 *
 * @author manoj.bardhan
 *
 */
public class SpringBootApplicationLuncher {
    public static void main(String[] args) {
        SpringApplication.run(HelloWorldController.class, args);
    }
}
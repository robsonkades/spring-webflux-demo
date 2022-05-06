package com.robsonkades.springwebflux;

import reactor.blockhound.BlockHound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringWebfluxApplication {

//    static {
//        BlockHound.install();
//    }

    public static void main(String[] args) {
        SpringApplication.run(SpringWebfluxApplication.class, args);
    }

}

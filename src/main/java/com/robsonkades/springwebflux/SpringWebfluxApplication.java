package com.robsonkades.springwebflux;

import reactor.blockhound.BlockHound;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

@SpringBootApplication
public class SpringWebfluxApplication {

    static {
        BlockHound
                .builder()
                .allowBlockingCallsInside("java.io.RandomAccessFile", "readBytes")
                .install();
    }

    public static void main(String[] args) {
        System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("admin"));
        SpringApplication.run(SpringWebfluxApplication.class, args);
    }

}

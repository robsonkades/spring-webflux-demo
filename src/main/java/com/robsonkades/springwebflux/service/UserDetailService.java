package com.robsonkades.springwebflux.service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.robsonkades.springwebflux.repository.UserRepository;

@RequiredArgsConstructor
@Service
public class UserDetailService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .log()
                .cast(UserDetails.class);
    }
}

package com.robsonkades.springwebflux.repository;

import reactor.core.publisher.Mono;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.robsonkades.springwebflux.domain.User;

public interface UserRepository extends ReactiveCrudRepository<User, Integer> {

    Mono<User> findByUsername(String username);
}

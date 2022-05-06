package com.robsonkades.springwebflux.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.robsonkades.springwebflux.domain.Anime;

public interface AnimeRepository extends ReactiveCrudRepository<Anime, Integer> {
}

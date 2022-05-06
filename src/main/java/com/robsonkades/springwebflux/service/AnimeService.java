package com.robsonkades.springwebflux.service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.robsonkades.springwebflux.domain.Anime;
import com.robsonkades.springwebflux.repository.AnimeRepository;

@Service
@RequiredArgsConstructor
public class AnimeService {

    private final AnimeRepository animeRepository;

    public Flux<Anime> findAll() {
        return animeRepository.findAll();
    }

    public Mono<Anime> findById(Integer id) {
        return animeRepository.findById(id)
                .switchIfEmpty(monoResponseStatusNotFound());
    }

    private <T> Mono<T> monoResponseStatusNotFound() {
        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Mono<Anime> save(Anime anime) {
        return animeRepository.save(anime);
    }

    public Mono<Void> update(Anime anime) {
        return findById(anime.getId())
                .flatMap(animeRepository::save)
                .then();
    }

    public Mono<Void> delete(Integer id) {
        return findById(id)
                .flatMap(animeRepository::delete);
    }
}

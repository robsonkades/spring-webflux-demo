package com.robsonkades.springwebflux.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.robsonkades.springwebflux.domain.Anime;
import com.robsonkades.springwebflux.service.AnimeService;

@RequiredArgsConstructor
@RequestMapping("animes")
@RestController
@Slf4j
public class AnimeController {

    private final AnimeService animeService;

    @GetMapping
    public Flux<Anime> listAll() {
        return animeService.findAll();
    }

    @GetMapping(path = "{id}")
    public Mono<Anime> finById(@PathVariable("id") Integer id) {
        return animeService.findById(id);
    }
}

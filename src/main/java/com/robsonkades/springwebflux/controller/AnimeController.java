package com.robsonkades.springwebflux.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.robsonkades.springwebflux.domain.Anime;
import com.robsonkades.springwebflux.service.AnimeService;

@RequiredArgsConstructor
@RequestMapping("animes")
@RestController
@Slf4j
@SecurityScheme(
        name = "Basic authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class AnimeController {

    private final AnimeService animeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(tags = {"anime"}, security = @SecurityRequirement(name = "Basic authentication"))
    public Flux<Anime> listAll() {
        return animeService.findAll();
    }

    @GetMapping(path = "{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(tags = {"anime"}, security = @SecurityRequirement(name = "Basic authentication"))
    public Mono<Anime> findById(@PathVariable("id") Integer id) {
        return animeService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(tags = {"anime"}, security = @SecurityRequirement(name = "Basic authentication"))
    public Mono<Anime> create(@Valid @RequestBody Anime anime) {
        return animeService.save(anime);
    }

    @PostMapping("batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(tags = {"anime"}, security = @SecurityRequirement(name = "Basic authentication"))
    public Flux<Anime> batch(@RequestBody List<Anime> anime) {
        return animeService.saveBatch(anime);
    }

    @PutMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(tags = {"anime"}, security = @SecurityRequirement(name = "Basic authentication"))
    public Mono<Void> update(@PathVariable() Integer id, @Valid @RequestBody Anime anime) {
        return animeService.update(anime.withId(id));
    }

    @DeleteMapping(path = "{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(tags = {"anime"}, security = @SecurityRequirement(name = "Basic authentication"))
    public Mono<Void> delete(@PathVariable() Integer id) {
        return animeService.delete(id);
    }
}

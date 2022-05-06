package com.robsonkades.springwebflux.util;

import lombok.Builder;

import com.robsonkades.springwebflux.domain.Anime;

@Builder
public class AnimeCreator {

    public static Anime createAnimeToBeSaved() {
        return Anime.builder()
                .name("Naruto")
                .build();
    }

    public static Anime createValidAnime() {
        return Anime.builder()
                .id(1)
                .name("Naruto")
                .build();
    }

    public static Anime updateAnime() {
        return Anime.builder()
                .id(1)
                .name("Naruto 2")
                .build();
    }
}

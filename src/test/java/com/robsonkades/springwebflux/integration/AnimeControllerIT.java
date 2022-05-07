package com.robsonkades.springwebflux.integration;

import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.robsonkades.springwebflux.domain.Anime;
import com.robsonkades.springwebflux.exception.CustomAttributes;
import com.robsonkades.springwebflux.repository.AnimeRepository;
import com.robsonkades.springwebflux.service.AnimeService;
import com.robsonkades.springwebflux.util.AnimeCreator;

@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import({ AnimeService.class, CustomAttributes.class })
public class AnimeControllerIT {

    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClient webTestClient;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound
                .builder()
                .allowBlockingCallsInside("java.io.RandomAccessFile", "readBytes")
                .install();
    }

    @Test
    public void testBlockHound() {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);
                return "";
            });
            Schedulers.parallel().schedule(task);
            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (Exception e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @BeforeEach
    public void each() {
        BDDMockito
                .when(animeRepository.findAll())
                .thenReturn(Flux.just(anime));

        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.just(anime));

        BDDMockito
                .when(animeRepository.save(AnimeCreator.createAnimeToBeSaved()))
                .thenReturn(Mono.just(anime));

        BDDMockito
                .when(animeRepository.delete(ArgumentMatchers.any(Anime.class)))
                .thenReturn(Mono.empty());

        BDDMockito
                .when(animeRepository.save(AnimeCreator.createValidAnime()))
                .thenReturn(Mono.empty());
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {
        webTestClient
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_Flavor2_ReturnFluxOfAnime_WhenSuccessful() {
        webTestClient
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findById_ReturnMonoOfAnime_WhenSuccessful() {
        webTestClient
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    public void findById_ReturnMonoOfError_WhenEmptyMonoIsReturned() {
        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.detail").isEqualTo("This is an error");
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_CreatesAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        webTestClient
                .post()
                .uri("/animes")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("save returns mono error with bad request when name is empty")
    public void save_ReturnsError_WhenNameIsEmpty() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");
        webTestClient
                .post()
                .uri("/animes/")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(animeToBeSaved))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void delete_RemovesAnime_WhenSuccessful() {
        webTestClient
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete returns mono error when anime does not exist")
    public void delete_ReturnsMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    @DisplayName("update save updated anime and return empty mono when successful")
    public void update_SaveUpdatedAnime_WhenSuccessful() {
        webTestClient
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(AnimeCreator.createValidAnime()))
                .exchange()
                .expectStatus().isNoContent();

    }

    @Test
    @DisplayName("update returns Mono error when anime does exist")
    public void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        webTestClient
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(AnimeCreator.createValidAnime()))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }
}

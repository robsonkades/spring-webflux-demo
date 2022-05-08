package com.robsonkades.springwebflux.integration;

import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import com.robsonkades.springwebflux.domain.Anime;
import com.robsonkades.springwebflux.repository.AnimeRepository;
import com.robsonkades.springwebflux.util.AnimeCreator;
import com.robsonkades.springwebflux.util.WebTestClientUtil;

@ExtendWith(SpringExtension.class)
//@WebFluxTest
//@Import({ AnimeService.class, CustomAttributes.class })
@SpringBootTest
@AutoConfigureWebTestClient
public class AnimeControllerIT {

    @MockBean
    private AnimeRepository animeRepository;

    @Autowired
    private WebTestClientUtil webTestClientUtil;

    @Autowired
    private WebTestClient client;
//    private WebTestClient webTestClientUser;
//    private WebTestClient webTestClientAdmin;
//    private WebTestClient webTestClientInvalid;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound
                .builder()
                .allowBlockingCallsInside("java.io.RandomAccessFile", "readBytes")
                .install();
    }

    @Test
    @Disabled("Ignored test after add SpringBootTest and AutoConfigureWebTestClient")
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
//        this.webTestClientUser = webTestClientUtil.authenticateClient("user", "admin");
//        this.webTestClientAdmin = webTestClientUtil.authenticateClient("admin", "admin");
//        this.webTestClientInvalid = webTestClientUtil.authenticateClient("xx", "xx");

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

        BDDMockito
                .when(animeRepository.saveAll(List.of(AnimeCreator.createAnimeToBeSaved(), AnimeCreator.createAnimeToBeSaved())))
                .thenReturn(Flux.just(anime, anime));
    }

    @Test
    @DisplayName("findAll returns a flux of anime when user is successfully authenticated and has role ADMIN")
    @WithUserDetails("admin")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {
        client
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(anime.getId())
                .jsonPath("$.[0].name").isEqualTo(anime.getName());
    }

    @Test
    @DisplayName("findAll returns forbidden when user is successfully authenticated and does not have role ADMIN")
    @WithUserDetails("user")
    //@WithMockUser(username = "admin", password = "admin", roles = {"USER"})
    public void findAll_ReturnForbidden_WhenUserDoesNotRoleAdmin() {
        client
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("findAll returns unauthorized when user is does not authenticated")
    public void findAll_ReturnUnauthorized_WhenUserIsNotAuthenticated() {
        client
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    @WithUserDetails("admin")
    public void findAll_Flavor2_ReturnFluxOfAnime_WhenSuccessful() {
        client
                .get()
                .uri("/animes")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Anime.class)
                .hasSize(1)
                .contains(anime);
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists and authenticated and has role ADMIN")
    @WithUserDetails("admin")
    public void findById_ReturnMonoOfAnime_WhenSuccessfulAndAuthenticatedWithRoleAdmin() {
        client
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }

    @Test
    @DisplayName("findById returns unauthorized with user  when it exists and not authenticated")
    public void findById_ReturnUnauthorized_WhenUserDoesNotAuthenticated() {
        client
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    @WithUserDetails("user")
    public void findById_ReturnMonoOfAnime_WhenSuccessful() {
        client
                .get()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Anime.class)
                .isEqualTo(anime);
    }



    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    @WithUserDetails("user")
    public void findById_ReturnMonoOfError_WhenEmptyMonoIsReturned() {
        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        client
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
    @WithUserDetails("admin")
    public void save_CreatesAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();
        client
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
    @DisplayName("saveBatch creates a list of anime when successful")
    @WithUserDetails("admin")
    public void saveBatch_CreatesListOfAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved)))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(Anime.class)
                .hasSize(2)
                .contains(anime, anime);
    }

    @Test
    @DisplayName("saveBatch returns a mono error when one of the objects in the list contains invalid name")
    @WithUserDetails("admin")
    public void saveBatch_ReturnsMonoError_WhenContainsInvalidName() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        BDDMockito
                .when(animeRepository.saveAll(ArgumentMatchers.anyIterable()))
                .thenReturn(Flux.just(anime, anime.withName("")));

        client
                .post()
                .uri("/animes/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(animeToBeSaved, animeToBeSaved.withName(""))))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("save returns mono error with bad request when name is empty")
    @WithUserDetails("admin")
    public void save_ReturnsError_WhenNameIsEmpty() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved().withName("");
        client
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
    @WithUserDetails("admin")
    public void delete_RemovesAnime_WhenSuccessful() {
        client
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @DisplayName("delete returns mono error when anime does not exist")
    @WithUserDetails("admin")
    public void delete_ReturnsMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        client
                .delete()
                .uri("/animes/{id}", 1)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404);
    }

    @Test
    @DisplayName("update save updated anime and return empty mono when successful")
    @WithUserDetails("admin")
    public void update_SaveUpdatedAnime_WhenSuccessful() {
        client
                .put()
                .uri("/animes/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(AnimeCreator.createValidAnime()))
                .exchange()
                .expectStatus().isNoContent();

    }

    @Test
    @DisplayName("update returns Mono error when anime does exist")
    @WithUserDetails("admin")
    public void update_ReturnMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        client
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

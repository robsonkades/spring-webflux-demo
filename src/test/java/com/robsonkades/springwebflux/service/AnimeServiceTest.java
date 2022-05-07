package com.robsonkades.springwebflux.service;

import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import com.robsonkades.springwebflux.domain.Anime;
import com.robsonkades.springwebflux.repository.AnimeRepository;
import com.robsonkades.springwebflux.util.AnimeCreator;

@ExtendWith(SpringExtension.class)
public class AnimeServiceTest {

    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository animeRepository;

    private final Anime anime = AnimeCreator.createValidAnime();

    @BeforeAll
    public static void blockHoundSetup() {
        BlockHound.install();
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
    }

    @Test
    @DisplayName("findAll returns a flux of anime")
    public void findAll_ReturnFluxOfAnime_WhenSuccessful() {
        StepVerifier.create(animeService.findAll())
                .expectSubscription()
                .expectNext(anime)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("findById returns Mono with anime when it exists")
    public void findById_ReturnMonoOfAnime_WhenSuccessful() {
        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectNext(anime)
                .expectComplete()
                .verify();
    }

    @Test
    @DisplayName("findById returns Mono error when anime does not exist")
    public void findById_ReturnMonoOfError_WhenEmptyMonoIsReturned() {
        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeService.findById(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save creates an anime when successful")
    public void save_CreatesAnime_WhenSuccessful() {
        Anime animeToBeSaved = AnimeCreator.createAnimeToBeSaved();

        StepVerifier.create(animeService.save(animeToBeSaved))
                .expectSubscription()
                .expectNext(anime)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete removes the anime when successful")
    public void delete_RemovesAnime_WhenSuccessful() {
        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns mono error when anime does not exist")
    public void delete_ReturnsMonoError_WhenEmptyMonoIsReturned() {
        BDDMockito
                .when(animeRepository.findById(ArgumentMatchers.anyInt()))
                .thenReturn(Mono.empty());

        StepVerifier.create(animeService.delete(1))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }
}

package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.port.in.RegisterSellerUseCase.Command;
import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Verifies that registering a seller runs its two writes (user and seller) inside
 * a single transaction, so a failure in the second write does not leave an
 * orphan user behind.
 *
 * <p>Traceability: OBJ-02, RN-04, RN-14, RN-15.</p>
 */
@SpringBootTest
@ActiveProfiles({"memory", "test"})
class SellerRegistrationTransactionTest {

    @Autowired
    private SellerRegistrationService service;

    @Autowired
    private RecordingRepositories repositories;

    @Test
    void registerSellerRunsBothWritesInTheSameTransaction() {
        repositories.transactionActiveDuringWrite.clear();

        service.registerSeller(new ActorContext(1L, Role.ADMINISTRATOR),
                new Command("Ana Torres", "ana@nexusmarket.com", "ID-001"));

        assertThat(repositories.transactionActiveDuringWrite)
                .as("one entry per write: user then seller")
                .containsExactly(true, true);
    }

    /** Shared recorder observed by both stubbed repositories. */
    static class RecordingRepositories {
        private final List<Boolean> transactionActiveDuringWrite = new ArrayList<>();
    }

    @TestConfiguration
    static class RecordingConfiguration {

        @Bean
        RecordingRepositories recordingRepositories() {
            return new RecordingRepositories();
        }

        @Bean
        @Primary
        UserRepository recordingUserRepository(RecordingRepositories recorder) {
            return new UserRepository() {
                @Override
                public User save(User user) {
                    recorder.transactionActiveDuringWrite
                            .add(TransactionSynchronizationManager.isActualTransactionActive());
                    user.setId(10L);
                    return user;
                }

                @Override
                public Optional<User> findById(Long id) {
                    return Optional.empty();
                }

                @Override
                public Optional<User> findByEmail(String email) {
                    return Optional.empty();
                }

                @Override
                public Optional<User> findByIdentityDocument(String identityDocument) {
                    return Optional.empty();
                }
            };
        }

        @Bean
        @Primary
        SellerRepository recordingSellerRepository(RecordingRepositories recorder) {
            return new SellerRepository() {
                @Override
                public Seller save(Seller seller) {
                    recorder.transactionActiveDuringWrite
                            .add(TransactionSynchronizationManager.isActualTransactionActive());
                    seller.setId(20L);
                    return seller;
                }

                @Override
                public Optional<Seller> findById(Long id) {
                    return Optional.empty();
                }

                @Override
                public Optional<Seller> findByUserId(Long userId) {
                    return Optional.empty();
                }
            };
        }
    }
}

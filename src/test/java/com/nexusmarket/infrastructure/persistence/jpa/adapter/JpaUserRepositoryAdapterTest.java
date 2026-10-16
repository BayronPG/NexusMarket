package com.nexusmarket.infrastructure.persistence.jpa.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import com.nexusmarket.infrastructure.persistence.jpa.repository.SpringDataUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Tests of {@link JpaUserRepositoryAdapter} against the embedded H2 database.
 *
 * <p>Fase 1.3.2 of docs/PLAN_API_WEB.md.</p>
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(JpaUserRepositoryAdapter.class)
class JpaUserRepositoryAdapterTest {

    @Autowired
    private JpaUserRepositoryAdapter adapter;

    @Autowired
    private SpringDataUserRepository springDataUserRepository;

    @BeforeEach
    void setUp() {
        springDataUserRepository.deleteAll();
    }

    private User newUser() {
        User user = new User();
        user.setFullName("Ada Lovelace");
        user.setEmail("ada@nexus.test");
        user.setIdentityDocument("DOC-1");
        user.setRole(Role.SELLER);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }

    @Test
    void saveAssignsIdAndReturnsDomain() {
        User saved = adapter.save(newUser());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("ada@nexus.test");
        assertThat(springDataUserRepository.findById(saved.getId())).isPresent();
    }

    @Test
    void saveUpdatesAnExistingUser() {
        User saved = adapter.save(newUser());

        saved.setFullName("Ada L. Updated");
        User updated = adapter.save(saved);

        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(springDataUserRepository.findById(saved.getId())
                .orElseThrow().getFullName()).isEqualTo("Ada L. Updated");
        assertThat(springDataUserRepository.count()).isEqualTo(1);
    }

    @Test
    void findByIdReturnsTheDomainWhenPresent() {
        User saved = adapter.save(newUser());

        assertThat(adapter.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(User::getEmail, User::getRole, User::getStatus)
                .containsExactly("ada@nexus.test", Role.SELLER, UserStatus.ACTIVE);
    }

    @Test
    void findByIdReturnsEmptyWhenAbsent() {
        assertThat(adapter.findById(999L)).isEmpty();
    }

    @Test
    void findByEmailReturnsTheDomainWhenPresent() {
        adapter.save(newUser());

        assertThat(adapter.findByEmail("ada@nexus.test"))
                .isPresent()
                .get()
                .extracting(User::getFullName)
                .isEqualTo("Ada Lovelace");
    }

    @Test
    void findByEmailReturnsEmptyWhenAbsent() {
        assertThat(adapter.findByEmail("nobody@nexus.test")).isEmpty();
    }

    @Test
    void findByIdentityDocumentReturnsTheDomainWhenPresent() {
        adapter.save(newUser());

        assertThat(adapter.findByIdentityDocument("DOC-1"))
                .isPresent()
                .get()
                .extracting(User::getEmail)
                .isEqualTo("ada@nexus.test");
    }

    @Test
    void findByIdentityDocumentReturnsEmptyWhenAbsent() {
        assertThat(adapter.findByIdentityDocument("DOC-UNKNOWN")).isEmpty();
    }
}

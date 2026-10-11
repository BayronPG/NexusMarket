package com.nexusmarket.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.nexusmarket.application.ActorContext;
import com.nexusmarket.application.error.AuthorizationException;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.ValidationException;
import com.nexusmarket.application.port.in.RegisterSellerUseCase.Command;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import com.nexusmarket.infrastructure.persistence.memory.InMemorySellerRepository;
import com.nexusmarket.infrastructure.persistence.memory.InMemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests of the seller registration use case.
 *
 * <p>Traceability: RN-04 (only the Administrator incorporates sellers),
 * RN-14 and RN-15 (unique identity document and email), RG-01 and RG-03.</p>
 */
class SellerRegistrationServiceTest {

    private InMemoryUserRepository users;
    private SellerRegistrationService service;

    private final ActorContext administrator = new ActorContext(1L, Role.ADMINISTRATOR);
    private final ActorContext seller = new ActorContext(2L, Role.SELLER);
    private final ActorContext buyer = new ActorContext(3L, Role.BUYER);

    @BeforeEach
    void setUp() {
        users = new InMemoryUserRepository();
        service = new SellerRegistrationService(users, new InMemorySellerRepository());
    }

    @Test
    void administratorRegistersSellerWithItsUser() {
        Seller created = service.registerSeller(administrator,
                new Command("Ana Torres", "ana@nexusmarket.com", "ID-001"));

        assertThat(created.getId()).isNotNull();
        User user = users.findById(created.getUserId()).orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.SELLER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getEmail()).isEqualTo("ana@nexusmarket.com");
    }

    @Test
    void sellerCannotSelfRegister() {
        assertThatThrownBy(() -> service.registerSeller(seller,
                new Command("Ana Torres", "ana@nexusmarket.com", "ID-001")))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void buyerCannotRegisterSeller() {
        assertThatThrownBy(() -> service.registerSeller(buyer,
                new Command("Ana Torres", "ana@nexusmarket.com", "ID-001")))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void unauthenticatedActorIsRejected() {
        assertThatThrownBy(() -> service.registerSeller(null,
                new Command("Ana Torres", "ana@nexusmarket.com", "ID-001")))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    void duplicatedEmailIsRejected() {
        service.registerSeller(administrator, new Command("Ana", "ana@nexusmarket.com", "ID-001"));

        assertThatThrownBy(() -> service.registerSeller(administrator,
                new Command("Luis", "ana@nexusmarket.com", "ID-002")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void duplicatedIdentityDocumentIsRejected() {
        service.registerSeller(administrator, new Command("Ana", "ana@nexusmarket.com", "ID-001"));

        assertThatThrownBy(() -> service.registerSeller(administrator,
                new Command("Luis", "luis@nexusmarket.com", "ID-001")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void blankFullNameIsRejected() {
        assertThatThrownBy(() -> service.registerSeller(administrator,
                new Command("   ", "ana@nexusmarket.com", "ID-001")))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void administratorFindsAnExistingSeller() {
        Seller created = service.registerSeller(administrator,
                new Command("Ana", "ana@nexusmarket.com", "ID-001"));

        assertThat(service.findSeller(administrator, created.getId()).getId()).isEqualTo(created.getId());
    }
}

package com.nexusmarket.application.service;

import com.nexusmarket.application.context.ActorContext;
import com.nexusmarket.application.error.BusinessRuleException;
import com.nexusmarket.application.error.NotFoundException;
import com.nexusmarket.application.port.in.RegisterSellerUseCase;
import com.nexusmarket.application.port.out.SellerRepository;
import com.nexusmarket.application.port.out.UserRepository;
import com.nexusmarket.application.support.Authorization;
import com.nexusmarket.application.support.Validation;
import com.nexusmarket.domain.seller.Seller;
import com.nexusmarket.domain.user.Role;
import com.nexusmarket.domain.user.User;
import com.nexusmarket.domain.user.UserStatus;
import org.springframework.stereotype.Service;

/**
 * Application service that registers sellers.
 *
 * <p>EXPLICIT rules: only the Administrator incorporates sellers (RN-04);
 * email and identity document are unique (RN-15, RN-14).</p>
 *
 * <p>INTERPRETATION: a newly registered seller starts with the ACTIVE status;
 * the exact catalog of user statuses is PENDING in the source.</p>
 *
 * <p>Traceability: OBJ-02, RN-04, RN-14, RN-15, RG-01, RG-03.</p>
 */
@Service
public class SellerRegistrationService implements RegisterSellerUseCase {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;

    public SellerRegistrationService(UserRepository userRepository, SellerRepository sellerRepository) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public Seller registerSeller(ActorContext actor, Command command) {
        Authorization.requireAnyRole(actor, Role.ADMINISTRATOR);
        Validation.requireNotNull(command, "command");
        Validation.requireText(command.fullName(), "fullName");
        Validation.requireText(command.email(), "email");
        Validation.requireText(command.identityDocument(), "identityDocument");

        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new BusinessRuleException("The email is already registered (RN-15)");
        }
        if (userRepository.findByIdentityDocument(command.identityDocument()).isPresent()) {
            throw new BusinessRuleException("The identity document is already registered (RN-14)");
        }

        User user = new User();
        user.setFullName(command.fullName());
        user.setEmail(command.email());
        user.setIdentityDocument(command.identityDocument());
        user.setRole(Role.SELLER);
        user.setStatus(UserStatus.ACTIVE);
        User savedUser = userRepository.save(user);

        Seller seller = new Seller();
        seller.setUserId(savedUser.getId());
        return sellerRepository.save(seller);
    }

    @Override
    public Seller findSeller(ActorContext actor, Long sellerId) {
        Authorization.requireAnyRole(actor, Role.ADMINISTRATOR);
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new NotFoundException("Seller not found: " + sellerId));
    }
}

package com.example.carrentalproject.service;

import com.example.carrentalproject.dto.UserInDto;
import com.example.carrentalproject.repository.UserRepository;
import com.example.carrentalproject.utils.PasswordValidator;
import net.jqwik.api.*;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for registration invariants.
 */
public class RegistrationServicePropertyTest {

    @Provide
    Arbitrary<String> strongPasswords() {
        // Must have at least 8, include lower, upper, digit
        Arbitrary<Character> lower = Arbitraries.chars().range('a', 'z');
        Arbitrary<Character> upper = Arbitraries.chars().range('A', 'Z');
        Arbitrary<Character> digits = Arbitraries.chars().range('0', '9');
        Arbitrary<String> tails = Arbitraries.strings().alpha().numeric().ofMinLength(5).ofMaxLength(12);
        return Combinators.combine(lower, upper, digits, tails).as((l,u,d,t) -> "" + l + u + d + t);
    }

    @Provide
    Arbitrary<String> weakPasswords() {
        // Missing upper or digit or too short
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(7);
    }

    @Property
    void passwordValidatorAcceptsStrong(@ForAll("strongPasswords") String pwd) {
        assertTrue(PasswordValidator.matcher(pwd).matches());
    }

    @Property
    void passwordValidatorRejectsWeak(@ForAll("weakPasswords") String pwd) {
        assertFalse(PasswordValidator.matcher(pwd).matches());
    }

    @Property
    void registerAcceptsAnyEmailFormatAsServiceDoesNotValidateEmail(
            @ForAll String email,
            @ForAll String username,
            @ForAll("strongPasswords") String password,
            @ForAll("phoneRange") Integer phone
    ) {
        if (username == null || username.isBlank()) return; // skip invalid
        
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = mock(UserService.class);
        RegistrationService registrationService = new RegistrationService(userRepository, passwordEncoder, userService);

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("enc");

        UserInDto dto = UserInDto.builder()
                .firstName("A")
                .lastName("B")
                .username(username)
                .password(password)
                .email(email)
                .phone(phone)
                .build();

        assertDoesNotThrow(() -> registrationService.registerUser(dto));
    }

    @Provide
    Arbitrary<Integer> phoneRange() {
        // Allow full integer range, including negatives
        return Arbitraries.integers().between(Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1);
    }

    @Property
    void duplicateUsernameAlwaysRejected(@ForAll String username,
                                         @ForAll("strongPasswords") String password) {
        if (username == null || username.isBlank()) return; // skip invalid
        
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        UserService userService = mock(UserService.class);
        RegistrationService registrationService = new RegistrationService(userRepository, passwordEncoder, userService);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(Mockito.mock(com.example.carrentalproject.domain.User.class)));

        UserInDto dto = UserInDto.builder()
                .firstName("A")
                .lastName("B")
                .username(username)
                .password(password)
                .email("x@y.z")
                .phone(123)
                .build();

        assertThrows(RuntimeException.class, () -> registrationService.registerUser(dto));
    }
}

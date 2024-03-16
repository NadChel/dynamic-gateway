package com.example.dynamicgateway.util;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Accessors(fluent = true)
class UriValidatorTest {
    @Getter
    private static final List<String> validPaths = List.of(
            "/path", "/path/goes-on", "/p@a$t^h", ""
    );
    @Getter
    private static final List<String> invalidPaths = List.of(
            "path", "path/", "/path/", "/path?", "/"
    );
    @Getter
    private static final List<String> validSchemes = List.of(
            "http://", "lalala://", "Ht+-t.p://"
    );
    @Getter
    private static final List<String> invalidSchemes = List.of(
            "1http://", "https:/", "https//", "https;//"
    );

    @ParameterizedTest
    @MethodSource("validPaths")
    void isValidPath_withValidPath_returnsTrue(String validPath) {
        assertThat(UriValidator.isValidPath(validPath)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidPaths")
    void isValidPath_withInvalidPath_returnsFalse(String invalidPath) {
        assertThat(UriValidator.isValidPath(invalidPath)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("validPaths")
    void requireValidPath_withValidPath_doesnThrow(String validPath) {
        assertThatCode(() -> UriValidator.requireValidPath(validPath)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidPaths")
    void requireValidPath_withInvalidPath_throwsRuntimeException(String invalidPath) {
        assertThatThrownBy(() -> UriValidator.requireValidPath(invalidPath)).isInstanceOf(RuntimeException.class);
    }

    @ParameterizedTest
    @MethodSource("validSchemes")
    void isValidScheme_withValidScheme_returnsTrue(String validScheme) {
        assertThat(UriValidator.isValidScheme(validScheme)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("invalidSchemes")
    void isValidScheme_withInvalidScheme_returnsFalse(String invalidScheme) {
        assertThat(UriValidator.isValidScheme(invalidScheme)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("validSchemes")
    void requireValidScheme_withValidScheme_doesntThrow(String validScheme) {
        assertThatCode(() -> UriValidator.requireValidScheme(validScheme)).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("invalidSchemes")
    void requireValidScheme_withInvalidScheme_throwsRuntimeException(String invalidScheme) {
        assertThatThrownBy(() -> UriValidator.requireValidScheme(invalidScheme)).isInstanceOf(RuntimeException.class);
    }
}
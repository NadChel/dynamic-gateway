package com.example.dynamicgateway.model.authorizationHeader;

import com.example.dynamicgateway.exception.AuthenticationSchemeNotFoundException;
import lombok.SneakyThrows;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;
import java.util.concurrent.Callable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class AuthorizationHeaderTest {
    @Test
    void fromString_forNullInput_returnsHeaderWithEmptySchemeAndCredentials() {
        AuthorizationHeader header = AuthorizationHeader.fromString(null);
        assertSoftly(soft -> {
            soft.assertThat(header.getScheme()).isEmpty();
            soft.assertThat(header.getCredentials()).isEmpty();
        });
    }

    @Test
    void fromString_forNonNullInput_splitsStringIntoSchemeAndCredentials() {
        String scheme = "basic";
        String credentials = "12345";
        AuthorizationHeader header = AuthorizationHeader.fromString(
                MessageFormat.format("{0} {1}", scheme, credentials));
        assertSoftly(soft -> {
            soft.assertThat(header.getScheme()).isEqualTo(scheme);
            soft.assertThat(header.getCredentials()).isEqualTo(credentials);
        });
    }

    @Test
    @SneakyThrows
    void fromString_extraSpaces_dontMatter() {
        String scheme = "basic";
        String credentials = "12345";
        Callable<AuthorizationHeader> headerCreationCallable = () -> AuthorizationHeader.fromString(
                MessageFormat.format("  {0}      {1}  ", scheme, credentials)
        );
        assertThatCode(headerCreationCallable::call).doesNotThrowAnyException();
        AuthorizationHeader header = headerCreationCallable.call();
        assertSoftly(soft -> {
            soft.assertThat(header.getScheme()).isEqualTo(scheme);
            soft.assertThat(header.getCredentials()).isEqualTo(credentials);
        });
    }

    @Test
    void fromString_forStringWithNoSpace_throws() {
        assertThatThrownBy(() -> AuthorizationHeader.fromString("invalid-header"))
                .isInstanceOf(AuthenticationSchemeNotFoundException.class);
    }

    @Test
    void equalsHashCodeContract() {
        EqualsVerifier.forClass(AuthorizationHeader.class).verify();
    }

    @Test
    void toString_evenIfStringPassedIntoConstructorContainedExtraSpaces_returnsSchemePlusOneSpacePlusCredentials() {
        String credentials = "12345";
        String spaces = "            ";
        AuthorizationHeader authorizationHeader = AuthorizationHeader.fromString(
                spaces + AuthorizationHeader.BEARER_SPACE +
                        spaces + credentials + spaces
        );
        assertThat(authorizationHeader.toString()).isEqualTo(MessageFormat.format(
                "{0} {1}", AuthorizationHeader.BEARER_SPACE.trim(), credentials
        ));
    }
}
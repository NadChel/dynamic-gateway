package com.example.dynamicgateway.model.authorizationHeader;

import com.example.dynamicgateway.exception.AuthenticationSchemeNotFoundException;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

import java.text.MessageFormat;

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
        AuthorizationHeader header = AuthorizationHeader.fromString(MessageFormat.format("{0} {1}", scheme, credentials));
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
}
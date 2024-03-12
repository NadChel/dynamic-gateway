package com.example.dynamicgateway.service.authenticationExtractor;

import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class BearerAuthorizationHeaderAuthenticationExtractorTest {
    @Test
    void isSupportedHeader_ignoresCase() {
        BearerAuthorizationHeaderAuthenticationExtractor extractor = bearerToken -> null;

        AuthorizationHeader lowerCaseHeader = AuthorizationHeader.fromString("bearer 12345");
        AuthorizationHeader upperCaseHeader = AuthorizationHeader.fromString("Bearer 12345");
        AuthorizationHeader chaoticCaseHeader = AuthorizationHeader.fromString("bEaREr 12345");
        List<AuthorizationHeader> matchingHeaders = List.of(lowerCaseHeader, upperCaseHeader, chaoticCaseHeader);

        assertSoftly(soft -> matchingHeaders.forEach(h -> assertHeaderMatches(soft, extractor, h)));
    }

    private void assertHeaderMatches(SoftAssertions soft, BearerAuthorizationHeaderAuthenticationExtractor extractor,
                                     AuthorizationHeader matchingHeader) {
        soft.assertThat(extractor.isSupportedAuthorizationHeader(matchingHeader))
                .withFailMessage(
                        "This matching header wasn't recognized as supported: " + matchingHeader)
                .isTrue();
    }
}
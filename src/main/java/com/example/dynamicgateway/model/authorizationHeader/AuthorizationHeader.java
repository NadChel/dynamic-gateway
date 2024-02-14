package com.example.dynamicgateway.model.authorizationHeader;

import com.example.dynamicgateway.exception.AuthenticationSchemeNotFoundException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class AuthorizationHeader {
    private final String scheme;
    private final String credentials;
    public AuthorizationHeader(String authorizationHeaderString) {
        try {
            String trimmedHeader = authorizationHeaderString.trim();
            int indexOfSpace = trimmedHeader.indexOf(" ");
            String schemeSubstring = trimmedHeader.substring(0, indexOfSpace);
            String credentialsSubstring = trimmedHeader.substring(indexOfSpace).trim();

            this.scheme = schemeSubstring;
            this.credentials = credentialsSubstring;
        } catch (StringIndexOutOfBoundsException e) {
            throw new AuthenticationSchemeNotFoundException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationHeader that = (AuthorizationHeader) o;
        return Objects.equals(scheme, that.scheme) && Objects.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheme, credentials);
    }
}

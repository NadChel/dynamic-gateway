package com.example.dynamicgateway.model.authorizationHeader;

import com.example.dynamicgateway.exception.AuthenticationSchemeNotFoundException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class AuthorizationHeader {
    public static final String BEARER_SPACE = "bearer ";
    public static final String BASIC_SPACE = "basic ";
    private final String scheme;
    private final String credentials;

    public AuthorizationHeader(String authorizationHeaderString) {
        String scheme, credentials;
        try {
            String trimmedHeader = authorizationHeaderString.trim();
            int indexOfSpace = trimmedHeader.indexOf(" ");
            scheme = trimmedHeader.substring(0, indexOfSpace);
            credentials = trimmedHeader.substring(indexOfSpace).trim();
        } catch (NullPointerException e) {
            scheme = "";
            credentials = "";
        } catch (StringIndexOutOfBoundsException e) {
            throw new AuthenticationSchemeNotFoundException();
        }
        this.scheme = scheme;
        this.credentials = credentials;
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

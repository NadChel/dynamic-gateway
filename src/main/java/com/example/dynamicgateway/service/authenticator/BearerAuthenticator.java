package com.example.dynamicgateway.service.authenticator;

import com.example.dynamicgateway.constant.JWT;
import com.example.dynamicgateway.model.authorizationHeader.AuthorizationHeader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

public class BearerAuthenticator extends Authenticator {
    public BearerAuthenticator(String credentials) {
        super(credentials);
    }
    public BearerAuthenticator(AuthorizationHeader header) {
        super(header);
    }

    @Override
    public String getHandledScheme() {
        return "bearer";
    }

    @Override
    public Authentication buildAuthentication() {
        Claims claims = getClaims(credentials);

        Object clientId = getPrincipalName(claims);
        List<? extends GrantedAuthority> authorities = getGrantedAuthorities(claims);

        return new UsernamePasswordAuthenticationToken(clientId, null, authorities);
    }

    private Claims getClaims(String jwt) {
        try {
            return doGetClaims(jwt);
        } catch (JwtException e) {
            throw new BadCredentialsException(
                    MessageFormat.format("Invalid token. {0}", e.getMessage()), e);
        }
    }

    private Claims doGetClaims(String jwt) {
        SecretKey hashedKey = Keys.hmacShaKeyFor(JWT.KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
                .setSigningKey(hashedKey)
                .build()
                .parseClaimsJws(jwt)
                .getBody();
    }

    private Object getPrincipalName(Claims claims) {
        return claims.get(JWT.SUB);
    }

    private List<? extends GrantedAuthority> getGrantedAuthorities(Claims claims) {
        Object nullableRoles = claims.get(JWT.ROLES);
        return (nullableRoles instanceof List<?> roles) ?
                buildGrantedAuthoritiesFrom(roles) :
                Collections.emptyList();
    }

    private List<SimpleGrantedAuthority> buildGrantedAuthoritiesFrom(List<?> roles) {
        return roles.stream()
                .map(Object::toString)
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}

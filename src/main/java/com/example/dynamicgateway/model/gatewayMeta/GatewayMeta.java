package com.example.dynamicgateway.model.gatewayMeta;

import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Holder of meta information relating to this Gateway
 */
@Component
@ConfigurationProperties(prefix = "gateway")
@Setter
@Getter
public final class GatewayMeta {
    private List<Server> servers;
    private String versionPrefix;
    private List<String> publicPatterns;
    private List<String> ignoredPatterns;
    private List<String> ignoredPrefixes;
}

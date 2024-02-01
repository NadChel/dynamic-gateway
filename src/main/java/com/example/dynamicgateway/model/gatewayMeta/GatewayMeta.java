package com.example.dynamicgateway.model.gatewayMeta;

import io.swagger.v3.oas.models.servers.Server;
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
public final class GatewayMeta {
    private List<Server> servers;
    private String versionPrefix;
    private String[] publicPatterns;
    private String[] ignoredPatterns;

    public List<Server> servers() {
        return this.servers;
    }

    public String versionPrefix() {
        return this.versionPrefix;
    }

    public String[] publicPatterns() {
        return this.publicPatterns;
    }

    public String[] ignoredPatterns() {
        return this.ignoredPatterns;
    }
}

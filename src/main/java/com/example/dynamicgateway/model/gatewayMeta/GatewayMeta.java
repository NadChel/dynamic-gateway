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

    public List<Server> getServers() {
        return this.servers;
    }

    public String getVersionPrefix() {
        return this.versionPrefix;
    }

    public String[] getPublicPatterns() {
        return this.publicPatterns;
    }

    public String[] getIgnoredPatterns() {
        return this.ignoredPatterns;
    }
}

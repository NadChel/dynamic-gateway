package com.example.dynamicgateway.model.gatewayMeta;

import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Holder of meta information relating to this Gateway
 */
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayMeta {
    @Value("${server.port}")
    private String port;
    @Setter
    @Getter
    private List<Server> servers = List.of(new Server().url("http://localhost:" + port));
    @Setter
    @Getter
    private String versionPrefix = "";
    @Setter
    @Getter
    private List<String> publicPatterns = List.of("/" + UUID.randomUUID());
    @Setter
    @Getter
    private List<String> ignoredPatterns = Collections.emptyList();
    @Setter
    @Getter
    private List<String> ignoredPrefixes = Collections.emptyList();
}

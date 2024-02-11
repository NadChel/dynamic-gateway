package com.example.dynamicgateway.model.gatewayMeta;

import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Holder of meta information relating to this Gateway
 */
@Component
@ConfigurationProperties(prefix = "gateway")
@Setter
@Getter
public class GatewayMeta {
    private List<Server> servers = Collections.emptyList();
    private String versionPrefix = "";
    private List<String> publicPatterns = Collections.emptyList();
    private List<String> ignoredPatterns = Collections.emptyList();
    private List<String> ignoredPrefixes = Collections.emptyList();
}

package by.afinny.apigateway.model.gatewayMeta;

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
    private String v1Prefix;
    private String[] publicEndpoints;

    public List<Server> servers() {
        return this.servers;
    }

    public String v1Prefix() {
        return this.v1Prefix;
    }

    public String[] publicEndpoints() {
        return this.publicEndpoints;
    }
}

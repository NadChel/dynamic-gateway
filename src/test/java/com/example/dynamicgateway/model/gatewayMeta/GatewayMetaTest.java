package com.example.dynamicgateway.model.gatewayMeta;

import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static com.example.dynamicgateway.model.gatewayMeta.GatewayMetaTest.IfServerSpecified.*;
import static com.example.dynamicgateway.model.gatewayMeta.GatewayMetaTest.IfServerSpecified.SERVER_URL;
import static org.assertj.core.api.Assertions.assertThat;

class GatewayMetaTest {
    static final short PORT = 7777;

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = GatewayMetaTestConfig.class)
    @TestPropertySource(properties = "server.port=" + PORT)
    class IfServerNotSpecified {
        @Autowired
        GatewayMeta gatewayMeta;

        @Test
        void ifNoServerSpecified_serversHasOneServerMatchingLocalhostServerPort() {
            List<Server> servers = gatewayMeta.getServers();
            assertThat(servers).hasSize(1);
            Server server = servers.get(0);
            assertThat(server.getUrl()).isEqualTo("http://localhost:" + PORT);
        }
    }

    @Nested
    @ExtendWith(SpringExtension.class)
    @ContextConfiguration(classes = GatewayMetaTestConfig.class)
    @TestPropertySource(properties = """
            server.port=""" + PORT + """
            
            gateway.servers[0].url=""" + SERVER_URL + """
            
            gateway.servers[0].description=""" + SERVER_DESCRIPTION + """
            """)
    class IfServerSpecified {
        static final String SERVER_URL = "http://localhost:9999";
        static final String SERVER_DESCRIPTION = "test-description";
        @Autowired
        GatewayMeta gatewayMeta;

        @Test
        void ifServerSpecified_serverNotOverwrittenWithDefault() {
            List<Server> servers = gatewayMeta.getServers();
            assertThat(servers).hasSize(1);
            Server actualServer = servers.get(0);
            Server expectedServer = new Server().url(SERVER_URL).description(SERVER_DESCRIPTION);
            assertThat(actualServer).isEqualTo(expectedServer);
        }
    }

    @Configuration
    @EnableConfigurationProperties(GatewayMeta.class)
    static class GatewayMetaTestConfig {
    }
}
package com.example.dynamicgateway.service.swaggerDocParser;

import io.swagger.v3.parser.core.models.SwaggerParseResult;

/**
 * Interface that represents objects capable of parsing Open API documentation.
 * Implementations should support both JSON and YML formats
 */
public interface OpenApiParser {
    SwaggerParseResult parse(String serializedDoc);
}

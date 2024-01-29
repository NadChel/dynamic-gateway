package com.example.dynamicgateway.service.swaggerDocParser;

import io.swagger.v3.parser.core.models.SwaggerParseResult;

public interface SwaggerDocParser {
    SwaggerParseResult parse(String serializedDoc);
}

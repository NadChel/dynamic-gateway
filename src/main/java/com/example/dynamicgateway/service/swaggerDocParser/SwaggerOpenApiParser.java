package com.example.dynamicgateway.service.swaggerDocParser;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

/**
 * An {@link OpenApiParser} that wraps an {@link OpenAPIV3Parser}
 */
public class SwaggerOpenApiParser implements OpenApiParser<SwaggerParseResult> {
    private final OpenAPIV3Parser delegate = new OpenAPIV3Parser();
    @Override
    public SwaggerParseResult parse(String serializedDoc) {
        return delegate.readContents(serializedDoc);
    }
}

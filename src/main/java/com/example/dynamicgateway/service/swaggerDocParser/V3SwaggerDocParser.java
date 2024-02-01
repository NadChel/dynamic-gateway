package com.example.dynamicgateway.service.swaggerDocParser;

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

public class V3SwaggerDocParser implements SwaggerDocParser {
    private final OpenAPIV3Parser delegate = new OpenAPIV3Parser();
    @Override
    public SwaggerParseResult parse(String serializedDoc) {
        return delegate.readContents(serializedDoc);
    }
}

package com.example.dynamicgateway.service.swaggerDocParser;

/**
 * Interface that represents objects capable of parsing Open API documentation.
 * Implementations should support both JSON and YML formats
 *
 * @param <D> type of the deserialized documentation object
 */
public interface OpenApiParser<D> {
    /**
     * Deserializes the provided string into a documentation object
     *
     * @param serializedDoc Open API documentation that could be either in the JSON or YML format
     * @return deserialized documentation object
     */
    D parse(String serializedDoc);
}

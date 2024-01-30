package com.example.dynamicgateway.service.swaggerDocParser;

import io.swagger.v3.parser.OpenAPIV3Parser;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class V3SwaggerDocParserTest {
    @Test
    void testParse() {
        V3SwaggerDocParser v3SwaggerDocParser = new V3SwaggerDocParser();
        OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
        String docString = getDocString();
        assertThat(v3SwaggerDocParser.parse(docString)).isEqualTo(openAPIV3Parser.readContents(docString));
    }

    private String getDocString() {
        return """
                {
                  "openapi": "3.0.1",
                  "info": {
                    "title": "Hello World API",
                    "version": "1.0"
                  },
                  "servers": [
                    {
                      "url": "https://localhost:8080",
                      "description": "Dynamic-Gateway"
                    }
                  ],
                  "security": [
                    {
                      "bearer-key": []
                    }
                  ],
                  "paths": {
                    "/api/v1/joy": {
                      "get": {
                        "tags": [
                          "Message Controller"
                        ],
                        "operationId": "getMessageOfJoy",
                        "responses": {
                          "200": {
                            "description": "OK",
                            "content": {
                              "*/*": {
                                "schema": {
                                  "$ref": "#/components/schemas/SuccessMessage",
                                  "exampleSetFlag": false
                                },
                                "exampleSetFlag": false
                              }
                            }
                          }
                        }
                      }
                    },
                    "/api/v1/hello-world": {
                      "get": {
                        "tags": [
                          "Message Controller"
                        ],
                        "operationId": "getHelloWorld",
                        "parameters": [
                          {
                            "name": "principal",
                            "in": "query",
                            "required": false,
                            "style": "FORM",
                            "explode": true,
                            "schema": {
                              "type": "string",
                              "exampleSetFlag": false,
                              "types": [
                                "string"
                              ]
                            }
                          },
                          {
                            "name": "roles",
                            "in": "query",
                            "required": false,
                            "style": "FORM",
                            "explode": true,
                            "schema": {
                              "type": "array",
                              "exampleSetFlag": false,
                              "items": {
                                "type": "string",
                                "exampleSetFlag": false,
                                "types": [
                                  "string"
                                ]
                              },
                              "types": [
                                "array"
                              ]
                            }
                          }
                        ],
                        "responses": {
                          "200": {
                            "description": "OK",
                            "content": {
                              "*/*": {
                                "schema": {
                                  "$ref": "#/components/schemas/SuccessMessage",
                                  "exampleSetFlag": false
                                },
                                "exampleSetFlag": false
                              }
                            }
                          }
                        }
                      }
                    }
                  },
                  "components": {
                    "schemas": {
                      "SuccessMessage": {
                        "type": "object",
                        "properties": {
                          "message": {
                            "type": "string",
                            "exampleSetFlag": false,
                            "types": [
                              "string"
                            ]
                          }
                        },
                        "exampleSetFlag": false,
                        "types": [
                          "object"
                        ]
                      },
                      "FailureMessage": {
                        "type": "object",
                        "properties": {
                          "message": {
                            "type": "string",
                            "exampleSetFlag": false,
                            "types": [
                              "string"
                            ]
                          },
                          "method": {
                            "type": "string",
                            "exampleSetFlag": false,
                            "types": [
                              "string"
                            ],
                            "enum": [
                              "GET",
                              "HEAD",
                              "POST",
                              "PUT",
                              "PATCH",
                              "DELETE",
                              "OPTIONS",
                              "TRACE"
                            ]
                          },
                          "request_path": {
                            "type": "string",
                            "exampleSetFlag": false,
                            "types": [
                              "string"
                            ]
                          }
                        },
                        "exampleSetFlag": false,
                        "types": [
                          "object"
                        ]
                      }
                    },
                    "securitySchemes": {
                      "bearer-key": {
                        "type": "HTTP",
                        "scheme": "bearer",
                        "bearerFormat": "JWT"
                      }
                    },
                    "extensions": {}
                  }
                }
                """;
    }
}
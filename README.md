# Table of contents
1. [About](#about)
2. [Configuration](#properties)
3. [Getting started](#getting%20started)
4. [Flow](#flow)

<a id="about"></a>
# About
**Dynamic Gateway** is a [Gateway pattern][] implementation that dynamically routes incoming requests to endpoints exposed by other applications connected to the same service discovery server. As services become available, this Dynamic Gateway automatically fetches their API documentation (assuming it's accessible) and builds a `Route` for each* exposed endpoint providing users a single point of entry. By the same token, if a service goes down, this Gateway will remove any `Route` associated with that service on the next refresh cycle. Such changes don't require a restart of this Gateway which dynamically updates its API in runtime

> **Route.** This Gateway implementation largely relies on Spring Framework and specifically Spring Cloud Gateway. One of its key abstractions is a so-called `Route` that represents a mapping between a request and its final destination. Essentially, a `Route` is a collection of predicates applied on a request. Once an aggregate predicate of a `Route` is satisfied by some request, that `Route`'s filters are applied to that request – potentially mutating it or replacing with another request – which is then (typically) redirected elsewhere
> 
> Though technically there's no one-to-one relationship between a `Route` and an endpoint (a `Route` can have a filter that changes the request path to different values based on some condition), in this application *each `Route` is associated with exactly one endpoint*. 
> 
> Whenever the word "route" is used in this README, it refers to an instance of Spring Cloud's `Route`

<small>* It's possible to filter out discovered endpoints preventing them from becoming part of this Gateway's API, see `EndpointSieve`</small>

[Gateway pattern]: https://microservices.io/patterns/apigateway.html

<a id="properties"></a>
# Configuration properties

Here is an overview of properties specific to this application

* `gateway.versionPrefix` – a prefix that will be appended to an endpoint's path when building a route. It is assumed by this application that such a prefix would specify an API version hence the name. For example, if it's set to `/api/v1` and some discovered service has endpoint `GET /example`, a route matching `GET /api/v1/example` will be built. Once it happens, this Gateway upon receiving a request `GET /api/v1/example` will route it to the service's `GET /example`. *Defaults to an empty string*
* `gateway.publicPatterns` – a list of [Ant-style path patterns][Ant patterns] that denote endpoints not requiring an authenticated user. Any user will be allowed to hit such an endpoint, no `Authorization` header required. *Defaults to an empty unmodifiable list*
* `gateway.ignoredPatterns` – a list of Ant path patterns which describe endpoints that shouldn't be mapped to this Gateway's routes. For example, if a service exposes endpoints `GET /example` and `GET /error`, and the list of ignored patterns includes `/error/**`, only one Route will be built, the one that routes to `GET /example`. *Defaults to an empty unmodifiable list*
* `gateway.ignoredPrefixes` – a list of endpoint prefixes that should be ignored when building routes. For example, if the list includes `/auth`, and the endpoint `GET /auth/example` was discovered, the route `GET <versionPrefix>/example` will be built (not `GET <versionPrefix>/auth/example`). *Defaults to an empty unmodifiable list*
* `gateway.servers` – a list of this Gateway's servers. This property is mainly for Swagger UI's dropdown menu.  *Defaults to an empty unmodifiable list* 

[Ant patterns]: https://docs.spring.io/spring-framework/docs/3.2.0.RELEASE_to_3.2.1.RELEASE/Spring%20Framework%203.2.1.RELEASE/org/springframework/util/AntPathMatcher.html

# Getting started

Here's the easiest way to launch this application
1. Clone it
```
git clone https://github.com/NadChel/dynamic-gateway
```
2. Locate to the project root
```
cd dynamic-gateway
```
3. Execute the `docker-compose` file
```
docker-compose up
```

The `docker-compose` file comprises images of the following applications:
* Dynamic Gateway
* an Eureka server ([Docker Hub][eureka])
* Token Service, a no-frills authentication server ([Docker Hub][token-service])
* a Postgres server, Token Service's data store ([Docker Hub][postgres])
* Hello World Service, a simple REST service ([Docker Hub][helloworld-service])

You may choose to run only some of those services by explicitly passing them as arguments. For example, if you want to run only Dynamic Gateway and the Eureka server, you may execute the following command:
```
docker-compose up gateway eureka
```
If you want Dynamic Gateway to pick up endpoints of your own service, it should meet these two requirements (unless you choose to reconfigure Dynamic Gateway):
1. It should provide its Open API at `GET /v3/api-docs`
2. It should be connected to the same Eureka server as this gateway

[eureka]: https://hub.docker.com/repository/docker/nadchel/eureka-server
[token-service]: https://hub.docker.com/repository/docker/nadchel/token-service
[postgres]: https://hub.docker.com/_/postgres
[helloworld-service]: https://hub.docker.com/repository/docker/nadchel/helloworld-service

# Domain models
To have a better understanding of this application, it's important to know its fundamental concepts

1. `DiscoverableApplication` – application that could be discovered via a service discovery mechanism such as Netflix Eureka. 

*Implementation: `EurekaDiscoverableApplication` which is a simplistic wrapper around `com.netflix.discovery.shared.Application`*

2. `DocumentedApplication` – application that publishes its API documentation. It holds a reference to a `EurekaDiscoverableApplication` object

*Implementation: `SwaggerApplication`*

3. `DocumentedEndpoint` – endpoint exposed by a `DocumentedApplication`, that is described in its API documentation. It holds a reference to a `DocumentedApplication` that declares it

*Implementation: `SwaggerEndpoint`*

4. `EndpointDetails` – actual endpoint information such as method and path

*Implementation: `SwaggerEndpointDetails`*

# Flow

Here's the basic flow of this application

1. `ApplicationCollector` finds and collects a `DiscoverableApplication`
2. `EndpointCollector` tries to fetch API documentation of the discovered application. If it succeeds, it parses the documentation object and collects all endpoints that were specified in it
3. `DynamicRouteLocator` builds a Route based on each of the newly found endpoints

# Security

This gateway assumes the existence of an external authentication server and only extracts user claims without verifying them
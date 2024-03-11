# Table of contents
1. [About](#about)
2. [Configuration](#configuration)
3. [Getting started](#getting-started)
4. [Domain models](#domain-models)
5. [Flow](#flow)
6. [Security](#security)

# About
**Dynamic Gateway** is a [Gateway pattern][] implementation that dynamically routes incoming requests to endpoints exposed by other applications connected to the same service discovery server. As services become available, Dynamic Gateway automatically fetches their API documentation (if it's accessible) and builds a `Route` for each* exposed endpoint providing users with a single point of entry. By the same token, if a service goes down, this Gateway will remove any `Route` associated with that service at the next refresh cycle. Such changes don't require a restart of this Gateway which dynamically updates its API in runtime

> **Route.** This Gateway implementation largely relies on Spring Framework and Spring Cloud Gateway in particular. One of its key abstractions is a so-called `Route` that represents a mapping between a request and its final destination. Essentially, a `Route` is a collection of predicates applied on a request. Once an aggregate predicate of a `Route` is satisfied by some request, that `Route`'s filters get applied to it – potentially mutating or replacing it with another request – which is then (typically) redirected elsewhere
> 
> Though technically there's no one-to-one relationship between a `Route` and an endpoint (a `Route` can have a filter that changes the request path to different values based on some condition, for example), in this application *each `Route` is associated with exactly one endpoint*. 
> 
> Whenever the word "route" is used in this README, it refers to an instance of `Route`

<small>* It's possible to filter out discovered endpoints preventing them from becoming part of this Gateway's API, see `EndpointSieve`</small>

[Gateway pattern]: https://microservices.io/patterns/apigateway.html

# Configuration

Here is an overview of properties specific to this application

* `gateway.versionPrefix` – a prefix that will be appended to an endpoint's path when building a route. It is assumed by this application that such a prefix would specify the API version hence the name. For example, if it's set to `/api/v1` and some discovered service has endpoint `GET /example`, a route matching `GET /api/v1/example` will be built. Once it happens, this Gateway upon receiving a request `GET /api/v1/example` will route it to the service's `GET /example`. *Defaults to an empty string*


* `gateway.publicPatterns` – a list of [Ant-style path patterns][Ant patterns] that specifies endpoints not requiring an authenticated user. *Defaults to a list of `/{random UUID}` which in effect doesn't match any request path*


* `gateway.ignoredPatterns` – a list of Ant path patterns which specifies endpoints that shouldn't be mapped to this Gateway's routes. For example, if a service exposes endpoints `GET /example` and `GET /error`, and the list of ignored patterns includes `/error/**`, only one Route will be built, the one that routes to `GET /example`. *Defaults to an empty unmodifiable list*


* `gateway.ignoredPrefixes` – a list of endpoint prefixes that should be ignored when building routes. For example, if the list includes `/auth`, and the endpoint `GET /auth/example` was discovered, the route `GET <versionPrefix>/example` will be built (not `GET <versionPrefix>/auth/example`). *Defaults to an empty unmodifiable list*


* `gateway.servers` – a list of this Gateway's servers. This property is mainly for Swagger UI's dropdown menu.  *Defaults to a list of `http://localhost:{server-port}`*


* `gateway.timeout` – timeout `Duration` used by Dynamic Gateway's [circuit breaker][]. If a service doesn't respond in the specified period of time, Dynamic Gateway will return a default fallback message. *Defaults to five seconds*

The properties are encapsulated by the `GatewayMeta` class

[Ant patterns]: https://docs.spring.io/spring-framework/docs/3.2.0.RELEASE_to_3.2.1.RELEASE/Spring%20Framework%203.2.1.RELEASE/org/springframework/util/AntPathMatcher.html
[circuit breaker]: https://microservices.io/patterns/reliability/circuit-breaker.html

<a id="getting-started"></a>
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
2. It should be connected to the same Eureka server as this Gateway

All services referenced in the `docker-compose` file, including Dynamic Gateway, expose their API via Swagger UI at `/swagger-ui`

[eureka]: https://hub.docker.com/repository/docker/nadchel/eureka-server
[token-service]: https://hub.docker.com/repository/docker/nadchel/token-service
[postgres]: https://hub.docker.com/_/postgres
[helloworld-service]: https://hub.docker.com/repository/docker/nadchel/helloworld-service

# Domain models
To have a better understanding of this application, it's important to know its fundamental concepts

1. `DiscoverableApplication` – application that could be discovered via a service discovery mechanism such as Netflix Eureka. *Implementation: `EurekaDiscoverableApplication` which is a simplistic wrapper around `com.netflix.discovery.shared.Application`*


2. `DocumentedApplication` – application that publishes its API documentation. It holds a reference to the associated `EurekaDiscoverableApplication` object. *Implementation: `SwaggerApplication`*


3. `DocumentedEndpoint` – endpoint exposed by a `DocumentedApplication`, that is described in its API documentation. It holds a reference to the `DocumentedApplication` that declares it. *Implementation: `SwaggerEndpoint`*


4. `EndpointDetails` – actual endpoint information such as the method and the request path. *Implementation: `SwaggerEndpointDetails`*

# Flow

Here's a high-level overview of the typical flow of this application

1. `ApplicationCollector` finds a `DiscoverableApplication` and applies its `ApplicationSieve`s on it (which are in effect `Predicate<DiscoverableApplication>`). If the application passes through, `ApplicationCollector` collects it


2. `EndpointCollector` tries to fetch API documentation of the application collected by `ApplicationCollector`. If it succeeds, it wraps the application and the doc in a `DocumentedApplication` instance, gets its `DocumentedEndpoint`s, applies `EndpointSieve`s on each of them, and collects all retained ones


3. `DynamicRouteLocator` creates a route from each endpoint collected by `EndpointCollector` by applying its `EndpointRouteProcessor`s on a fresh `Route` builder before calling `build()` on it. The job of a `EndpointRouteProcessor` is to take a route builder along with a `DocumentedEndpoint` and return a potentially mutated builder back to the caller. For example, an `EndpointRouteProcessor` may get the endpoint's HTTP method and then add a predicate matching that method to the builder before returning it

> **RouteLocator**, which `DynamicRouteLocator` implements, is another key abstraction of Spring Cloud Gateway. It's a functional interface that returns a `Flux<Route>` on `getRoutes()` invocations. An application may have multiple registered `RouteLocator`s, though Dynamic Gateway has only one

Here's what happens when a `DiscoverableApplication` goes down. In the case of `EurekaDiscoverableApplication` it means an application that was once in a Eureka client cache is no longer there

1. `ApplicationCollector` removes the application from its collection
2. `EndpoingCollector` removes all endpoints owned by the lost application from *its* collection
3. `DynamicRouteLocator` removes all routes whose URI's hosts match the lost app's name. For instance, if `SOME-APP` is lost, all routes with URI `lb://SOME-APP` will be evicted (assuming the lost app was an instance of `EurekaDiscoverableApplication` which uses `lb://` schemes)

Out-of-the-box implementations of these three key types – `ApplicationCollector`, `EndpointCollector`, and `DynamicRouteLocator` – relay `ApplicationEvent`s to communicate that information between each other

# Security

This gateway assumes the existence of an external authentication server and, as is, only extracts user claims contained in the request's JSON Web Token without authenticating them. It validates the token by checking its signing key

The two most important types involved in this process are `AuthenticationExtractionWebFilter` and `AuthenticationExtractor`. `AuthenticationExtractionWebFilter` is a `WebFilter` that extracts authentication claims and puts them in the `ReactiveSecurityContextHolder`. The filter delegates the extraction to the injected `AuthenticationExtractor`. By default, it's an instance of `CompositeAuthenticationExtractor` that has a collection of other `AuthenticationExtractor`s and delegates the actual extraction to the first of them that *may* extract claims from a given exchange

If you chose to use your own JWTs instead of the ones issued by Token Service, make sure the tokens are signed with a key this gateway expects

You may also create your own `AuthenticationExtractor` implementation and register it in the Spring context. For example, if you want to add support for other authentication schemes, other than `bearer`, you may implement `AuthorizationHeaderAuthenticationExtractor` and override the `tryExtractAuthentication(AuthorizationHeader)` and `isSupportedAuthorizationHeader(AuthorizationHeader)` methods. `AuthorizationHeader` is a simple data class that encapsulates the header's scheme and credentials and exposes convenient accessors for those properties

See the Javadocs for more information
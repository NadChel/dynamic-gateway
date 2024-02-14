# Table of contents
1. [About](#about)
2. [Configuration](#properties)
3. [Getting started](#getting%20started)
4. [Flow](#flow)

<a id="about"></a>
# About
**Dynamic Gateway** is a [Gateway pattern][] implementation that dynamically routes incoming requests to endpoints exposed by other applications connected to the same service discovery server. As services become available, this Dynamic Gateway automatically fetches their API documentation (assuming it's accessible) and builds a *Route* for each* exposed endpoint providing users a single point of entry. By the same token, if a service goes down, this Gateway will remove any Route associated with that service on the next refresh cycle. Such changes don't require a restart of this Gateway which dynamically updates its API in runtime

> **Route** This Gateway implementation largely relies on Spring Framework and specifically Spring Cloud Gateway. One of its key abstractions is a so-called Route that represents a mapping between a request and its final destination. Essentially, a Route is a collection of predicates applied on a request. Once an aggregate predicate of a Route is satisfied by some request, that Route's filters get to applied to that request, potentially mutating it, which is then (typically) redirected elsewhere. Though technically there's no one-to-one relationship between a Route and an endpoint (a Route can have a filter that changes request path to different values based on some condition), in this application each Route is associated with exactly one endpoint

<small>* It's possible to filter out discovered endpoints preventing them from becoming part of this Gateway's API</small>

[Gateway pattern]: https://microservices.io/patterns/apigateway.html

<a id="properties"></a>
# Configuration properties

Here is an overview of properties specific to this application

* `gateway.versionPrefix` – a prefix that will be appended to an endpoint's path when building a Route. Typically, such a prefix specifies an API version hence the name. For example, if it's set to `/api/v1` and some discovered service has endpoint `GET /example`, Route `GET /api/v1/example` will be built. Once it happens, this Gateway upon receiving request `GET /api/v1/example` will route it to the service's `GET /example`. *Defaults to an empty string*
* `gateway.publicPatterns` – a list of [Ant-style path patterns][Ant patterns] that denote endpoints not requiring an authenticated user. Any user will be allowed to hit such an endpoint, no `Authorization` header required
* `gateway.ignoredPatterns` – a list of Ant path patterns which describe endpoints that shouldn't be mapped to this Gateway's routes. For example, if a service exposes endpoints `GET /example` and `GET /error`, and the list of ignored patterns includes `/error/**`, only one Route will be built, the one that routes to `GET /example`
* `gateway.ignoredPrefixes` – a list of endpoint prefixes that should be ignored when building Routes. For example, if the list includes `/auth`, and endpoint `GET /auth/example` was discovered, Route `GET <versionPrefix>/example` will be built (not `GET <versionPrefix>/auth/example`)
* `gateway.servers` – a list of this Gateway's servers. This property is mainly for Swagger UI's dropdown menu 

[Ant patterns]: https://docs.spring.io/spring-framework/docs/3.2.0.RELEASE_to_3.2.1.RELEASE/Spring%20Framework%203.2.1.RELEASE/org/springframework/util/AntPathMatcher.html

# Getting started

The easiest way to launch this application is to clone it, locate to the project root and execute the `docker-compose` file. It includes a simple Eureka server image. The Eureka image may not be fit for production environment since it has the "self-preservation" mode disabled

# Domain models

`DiscoverableApplication` – application that could be discovered via a service discovery mechanism such as Netflix Eureka

`DocumentedApplication` – application that publishes its API documentation

`DocumentedEndpoint` – endpoint exposed by a `DocumentedApplication`, that is described in its API documentation

# Flow

Here's the basic flow of this application

1. `ApplicationCollector` finds and collects a `DiscoverableApplication`
2. `EndpointCollector` tries to fetch API documentation of the discovered application. If it succeeds, it parses the documentation object and collects all endpoints that were specified in it
3. `DynamicRouteLocator` builds a Route based on each of the newly found endpoints
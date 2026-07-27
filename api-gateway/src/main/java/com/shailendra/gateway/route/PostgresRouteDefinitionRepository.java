package com.shailendra.gateway.route;

import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class PostgresRouteDefinitionRepository implements RouteDefinitionRepository {
    private final DatabaseClient databaseClient;
    private final ApplicationEventPublisher publisher;

    public PostgresRouteDefinitionRepository(DatabaseClient databaseClient, ApplicationEventPublisher publisher) {
        this.databaseClient = databaseClient;
        this.publisher = publisher;
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return databaseClient.sql("SELECT id, path_pattern, target_uri FROM gateway_routes WHERE enabled = TRUE")
                .map((row, metadata) -> {
                    RouteDefinition definition = new RouteDefinition();
                    definition.setId(row.get("id", String.class));
                    definition.setUri(URI.create(row.get("target_uri", String.class)));
                    PredicateDefinition predicate = new PredicateDefinition();
                    predicate.setName("Path");
                    predicate.addArg("pattern", row.get("path_pattern", String.class));
                    definition.getPredicates().add(predicate);
                    FilterDefinition circuitBreaker = new FilterDefinition();
                    circuitBreaker.setName("CircuitBreaker");
                    circuitBreaker.addArg("name", definition.getId() + "CircuitBreaker");
                    circuitBreaker.addArg("fallbackUri", "forward:/fallback/generic");
                    definition.getFilters().add(circuitBreaker);
                    return definition;
                }).all();
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.flatMap(definition -> {
            String pattern = definition.getPredicates().stream()
                    .filter(p -> "Path".equals(p.getName()))
                    .findFirst()
                    .map(p -> p.getArgs().values().stream().findFirst().orElse("/**"))
                    .orElse("/**");
            return databaseClient.sql("""
                    INSERT INTO gateway_routes(id, path_pattern, target_uri, enabled)
                    VALUES (:id, :path, :uri, TRUE)
                    ON CONFLICT (id) DO UPDATE SET path_pattern=:path, target_uri=:uri, enabled=TRUE
                    """)
                    .bind("id", definition.getId())
                    .bind("path", pattern)
                    .bind("uri", definition.getUri().toString())
                    .fetch().rowsUpdated().then();
        }).doOnSuccess(v -> publisher.publishEvent(new RefreshRoutesEvent(this)));
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.flatMap(id -> databaseClient.sql("DELETE FROM gateway_routes WHERE id=:id")
                .bind("id", id).fetch().rowsUpdated().then())
                .doOnSuccess(v -> publisher.publishEvent(new RefreshRoutesEvent(this)));
    }
}

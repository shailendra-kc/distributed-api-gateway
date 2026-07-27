package com.shailendra.gateway.route;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("/admin/routes")
public class RouteAdminController {
    private final GatewayRouteRepository repository;
    private final ApplicationEventPublisher publisher;

    public RouteAdminController(GatewayRouteRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public record RouteRequest(@NotBlank String id, @NotBlank String pathPattern,
                               @NotBlank String targetUri, Boolean enabled) {}

    @GetMapping
    public Flux<GatewayRoute> all() {
        return repository.findAll();
    }

    @PostMapping
    public Mono<ResponseEntity<GatewayRoute>> create(@Valid @RequestBody RouteRequest request) {
        GatewayRoute route = new GatewayRoute(request.id(), request.pathPattern(), request.targetUri(),
                request.enabled() == null || request.enabled(), Instant.now());
        return repository.save(route)
                .doOnSuccess(saved -> publisher.publishEvent(new RefreshRoutesEvent(this)))
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return repository.deleteById(id)
                .doOnSuccess(v -> publisher.publishEvent(new RefreshRoutesEvent(this)))
                .thenReturn(ResponseEntity.noContent().build());
    }
}

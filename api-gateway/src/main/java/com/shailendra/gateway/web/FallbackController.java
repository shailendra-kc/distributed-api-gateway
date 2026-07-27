package com.shailendra.gateway.web;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import reactor.core.publisher.Mono; import java.time.Instant; import java.util.Map;
@RestController
public class FallbackController { @RequestMapping("/fallback/products") public Mono<ResponseEntity<Map<String,Object>>> productFallback(){return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("status",503,"message","Product service is temporarily unavailable","timestamp",Instant.now().toString())));} }

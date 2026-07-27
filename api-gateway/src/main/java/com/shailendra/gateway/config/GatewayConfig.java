package com.shailendra.gateway.config;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
@Configuration
public class GatewayConfig {
 @Bean public KeyResolver clientKeyResolver(){ return exchange -> Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("X-Client-Id")).switchIfEmpty(Mono.just(exchange.getRequest().getRemoteAddress()!=null?exchange.getRequest().getRemoteAddress().getAddress().getHostAddress():"anonymous")); }
}

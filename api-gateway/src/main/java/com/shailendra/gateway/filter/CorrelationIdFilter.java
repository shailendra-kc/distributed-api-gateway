package com.shailendra.gateway.filter;
import org.springframework.cloud.gateway.filter.*; import org.springframework.core.Ordered; import org.springframework.stereotype.Component; import org.springframework.web.server.ServerWebExchange; import reactor.core.publisher.Mono; import java.util.UUID;
@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
 public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain){String id=exchange.getRequest().getHeaders().getFirst("X-Correlation-Id"); if(id==null||id.isBlank()) id=UUID.randomUUID().toString(); String cid=id; exchange.getResponse().getHeaders().set("X-Correlation-Id",cid); return chain.filter(exchange.mutate().request(r->r.header("X-Correlation-Id",cid)).build());}
 public int getOrder(){return -200;}
}

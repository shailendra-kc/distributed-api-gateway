package com.shailendra.gateway.filter;
import org.slf4j.*; import org.springframework.cloud.gateway.filter.*; import org.springframework.core.Ordered; import org.springframework.stereotype.Component; import org.springframework.web.server.ServerWebExchange; import reactor.core.publisher.Mono;
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered { private static final Logger log=LoggerFactory.getLogger(RequestLoggingFilter.class);
 public Mono<Void> filter(ServerWebExchange ex,GatewayFilterChain chain){long start=System.currentTimeMillis(); String cid=ex.getRequest().getHeaders().getFirst("X-Correlation-Id"); log.info("request method={} path={} correlationId={}",ex.getRequest().getMethod(),ex.getRequest().getURI().getPath(),cid); return chain.filter(ex).doFinally(s->log.info("response status={} durationMs={} correlationId={}",ex.getResponse().getStatusCode(),System.currentTimeMillis()-start,cid));}
 public int getOrder(){return -50;}
}

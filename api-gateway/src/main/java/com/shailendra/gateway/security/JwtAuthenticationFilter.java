package com.shailendra.gateway.security;
import com.fasterxml.jackson.databind.ObjectMapper; import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value; import org.springframework.cloud.gateway.filter.*; import org.springframework.core.Ordered; import org.springframework.http.*; import org.springframework.stereotype.Component; import org.springframework.web.server.ServerWebExchange; import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets; import java.util.*;
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
 private final JwtService jwtService; private final ObjectMapper mapper; private final List<String> publicPaths;
 public JwtAuthenticationFilter(JwtService jwtService,ObjectMapper mapper,@Value("${security.public-paths}") String paths){this.jwtService=jwtService;this.mapper=mapper;this.publicPaths=Arrays.stream(paths.split(",")).map(String::trim).toList();}
 public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain){
  String path=exchange.getRequest().getPath().value(); if(publicPaths.stream().anyMatch(path::startsWith)) return chain.filter(exchange);
  String header=exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
  if(header==null||!header.startsWith("Bearer ")) return unauthorized(exchange,"Missing bearer token");
  try { Claims c=jwtService.parse(header.substring(7)); ServerWebExchange mutated=exchange.mutate().request(r->r.headers(h->{h.set("X-Authenticated-User",c.getSubject()); h.set("X-User-Roles",String.valueOf(c.get("roles")));})).build(); return chain.filter(mutated); }
  catch(Exception e){ return unauthorized(exchange,"Invalid or expired token"); }
 }
 private Mono<Void> unauthorized(ServerWebExchange exchange,String message){ try{byte[] b=mapper.writeValueAsBytes(Map.of("status",401,"error","Unauthorized","message",message)); exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED); exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON); return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(b)));}catch(Exception e){return exchange.getResponse().setComplete();}}
 public int getOrder(){return -100;}
}

package com.shailendra.gateway.route;
import org.springframework.data.annotation.Id; import org.springframework.data.relational.core.mapping.Table; import java.time.Instant;
@Table("gateway_routes") public record GatewayRoute(@Id String id,String pathPattern,String targetUri,Boolean enabled,Instant createdAt) {}

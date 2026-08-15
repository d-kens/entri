package com.entri.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class RateLimiterConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient redisClient(
            @Value("${rate-limit.redis.host}") String host,
            @Value("${rate-limit.redis.port}") int port
    ) {
        return RedisClient.create(RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withSsl(false)
                .build());
    }

    @Bean
    public ProxyManager<String> proxyManager(RedisClient redisClient) {
        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
        );
        return Bucket4jLettuce.casBasedBuilder(connection).build();
    }

    @Bean
    public Supplier<BucketConfiguration> bucketConfiguration(
            @Value("${rate-limit.reservation.capacity}") long capacity,
            @Value("${rate-limit.reservation.refill-period-seconds}") long refillPeriodSeconds
    ) {
        return () -> BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(capacity)
                        .refillIntervally(capacity, Duration.ofSeconds(refillPeriodSeconds))
                        .build())
                .build();
    }
}

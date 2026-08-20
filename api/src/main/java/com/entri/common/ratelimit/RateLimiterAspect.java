package com.entri.common.ratelimit;

import com.entri.common.exception.TooManyRequestsException;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.function.Supplier;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimiterAspect {

    private final ProxyManager<String> proxyManager;
    private final Supplier<BucketConfiguration> bucketConfiguration;

    @Around("@annotation(com.entri.common.ratelimit.RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String clientKey = resolveClientIp(request);

        Bucket bucket = proxyManager.builder().build(clientKey, bucketConfiguration);

        if (bucket.tryConsume(1)) {
            return joinPoint.proceed();
        }

        throw new TooManyRequestsException("You have exceeded the reservation request limit. Please try again later.");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

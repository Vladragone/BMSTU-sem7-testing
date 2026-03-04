package com.example.game.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenTelemetryConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(name = "tracing.enabled", havingValue = "true")
    public OpenTelemetrySdk openTelemetrySdk(
            @Value("${tracing.otlp.endpoint:http://localhost:4317}") String otlpEndpoint,
            @Value("${tracing.service-name:game-app}") String serviceName,
            @Value("${tracing.sample-ratio:1.0}") double sampleRatio,
            @Value("${tracing.exporter:otlp}") String exporterType) {

        Resource resource = Resource.getDefault().toBuilder()
                .put(AttributeKey.stringKey("service.name"), serviceName)
                .build();

        SpanProcessor spanProcessor = BatchSpanProcessor.builder(resolveExporter(exporterType, otlpEndpoint)).build();
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .setSampler(Sampler.traceIdRatioBased(sampleRatio))
                .setResource(resource)
                .addSpanProcessor(spanProcessor)
                .build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .build();
    }

    private io.opentelemetry.sdk.trace.export.SpanExporter resolveExporter(String exporterType, String otlpEndpoint) {
        if ("logging".equalsIgnoreCase(exporterType)) {
            return new LoggingSpanExporter();
        }
        return OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(OpenTelemetry.class)
    public OpenTelemetry noopOpenTelemetry() {
        return OpenTelemetry.noop();
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("com.example.game.http");
    }
}

package br.edu.atitus.bompradev.catalog.configs;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.logback.appender.v1_0.OpenTelemetryAppender;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class ConfigTelemetria implements InitializingBean {
    private final OpenTelemetry telemetria;

    public ConfigTelemetria(OpenTelemetry telemetria) { this.telemetria = telemetria; }

    @Override
    public void afterPropertiesSet() { OpenTelemetryAppender.install(telemetria); }
}

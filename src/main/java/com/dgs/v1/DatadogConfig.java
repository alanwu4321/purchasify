package com.dgs.v1;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.datadog.DatadogMeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class DatadogConfig extends Metrics {
       @Override
       public Duration step() {
           return Duration.ofSeconds(10);
       }

       @Override
       public String get(String k) {
           return null; // accept the rest of the defaults
       }
   MeterRegistry registry = new DatadogMeterRegistry(config, Clock.SYSTEM);
}

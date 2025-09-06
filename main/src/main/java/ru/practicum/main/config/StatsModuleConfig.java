package ru.practicum.main.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "ru.practicum.stats")
public class StatsModuleConfig {
}

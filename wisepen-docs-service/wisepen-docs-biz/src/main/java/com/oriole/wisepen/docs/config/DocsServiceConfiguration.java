package com.oriole.wisepen.docs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(DocsServiceProperties.class)
public class DocsServiceConfiguration {
}

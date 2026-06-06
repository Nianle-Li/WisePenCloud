package com.oriole.wisepen.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "wisepen.openapi")
public class WisepenOpenApiProperties {

    private String title;

    private String version = "1.0.0";

    private String description;
}

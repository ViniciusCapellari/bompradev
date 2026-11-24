package br.edu.atitus.bompradev.scheduling.configs;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.webclient.WebClientCustomizer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ConfigCatalogo {
    @Bean
    @LoadBalanced
    WebClient.Builder catalogoWebClient(ObjectProvider<WebClientCustomizer> customizadores) {
        var builder = WebClient.builder();
        customizadores.orderedStream().forEach(customizador -> customizador.customize(builder));
        return builder;
    }
}

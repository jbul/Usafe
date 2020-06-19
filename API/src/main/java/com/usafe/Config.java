package com.usafe;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetAddress;

@Configuration
public class Config {

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String clusterName;

    @Value("${elastic.search.cluster.address}")
    private String clusterAddress;


    @Bean
    Client client() throws IOException {
        Settings settings = Settings.builder()
                .put("cluster.name", clusterName)
                .build();
        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(clusterAddress)
                , 9300));
        return client;
    }
}

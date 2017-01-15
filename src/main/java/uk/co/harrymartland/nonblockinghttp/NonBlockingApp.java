package uk.co.harrymartland.nonblockinghttp;

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.client.HttpAsyncClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NonBlockingApp {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(NonBlockingApp.class, args);
    }

    @Bean
    public HttpAsyncClient httpAsyncClient() {
        CloseableHttpAsyncClient client = HttpAsyncClientBuilder
                .create()
                .setMaxConnPerRoute(Integer.MAX_VALUE)
                .setMaxConnTotal(Integer.MAX_VALUE)
                .build();

        client.start();
        return client;
    }

}

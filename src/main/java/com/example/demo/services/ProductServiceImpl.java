package com.example.demo.services;

import com.example.demo.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.synchronoss.cloud.nio.multipart.Multipart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private WebClient.Builder client;

    @Override
    public Flux<Product> findAll() {
        return client.build().get().accept(MediaType.APPLICATION_JSON)
                .exchange()
                .flatMapMany(response -> response.bodyToFlux(Product.class));
    }

    @Override
    public Mono<Product> findById(String id) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id",id);

        return client.build().get().uri("/{id}",params).accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Product.class);
    }

    @Override
    public Mono<Product> save(Product product) {
        return client.build().post().accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                //.body(BodyInserters.fromObject(product))
                .syncBody(product)
                .retrieve().bodyToMono(Product.class);
    }

    @Override
    public Mono<Product> update(Product product, String id) {
        return client.build().put().uri("/{id}", Collections.singletonMap("id",id))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(product)
                .retrieve()
                .bodyToMono(Product.class);
    }

    @Override
    public Mono<Void> delete(String id) {
        return client.build().delete().uri("/{id}", Collections.singletonMap("id",id))
                //.exchange()
                //.then();
                .retrieve()
                .bodyToMono(Void.class);
    }

    @Override
    public Mono<Product> upload(FilePart filePart, String id) {
        MultipartBodyBuilder multipartBodyBuilder = new MultipartBodyBuilder();

        multipartBodyBuilder.asyncPart("filePart", filePart.content(), DataBuffer.class)
                .headers(h -> {
                    h.setContentDispositionFormData("filePart", filePart.filename());
                });

        return client.build().post().uri("/upload/{id}", Collections.singletonMap("id", id))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .syncBody(multipartBodyBuilder.build())
                .retrieve()
                .bodyToMono(Product.class);
    }
}

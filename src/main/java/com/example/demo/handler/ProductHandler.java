package com.example.demo.handler;

import com.example.demo.models.Product;
import com.example.demo.services.ProductService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductHandler {

    @Autowired
    private ProductService productService;

    public Mono<ServerResponse> list(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(productService.findAll(), Product.class);
    }

    public Mono<ServerResponse> see(ServerRequest request) {
        String id = request.pathVariable("id");
        return productService.findById("id").flatMap(p -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(p)
                .switchIfEmpty(ServerResponse.notFound().build())
                .onErrorResume(ex -> {
                    WebClientResponseException error = (WebClientResponseException) ex;
                    if (error.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        //return ServerResponse.notFound().build();
                        Map<String, Object> body = new HashMap<>();
                        body.put("error", "No existe el producto".concat(ex.getMessage()));
                        body.put("TimeStamp",new Date());
                        return ServerResponse.status(HttpStatus.NOT_FOUND).syncBody(body);
                    }
                    return Mono.error(error);
                }));
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);

        return productMono.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return productService.save(p);
        }).flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(p))
                .onErrorResume(ex -> {
                    WebClientResponseException error = (WebClientResponseException) ex;
                    if (error.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.badRequest().contentType(MediaType.APPLICATION_JSON)
                                .syncBody(error.getResponseBodyAsString());
                    }
                    return Mono.error(error);
                });
    }

    public Mono<ServerResponse> edit(ServerRequest request) {
        Mono<Product> productMono = request.bodyToMono(Product.class);
        String id = request.pathVariable("id");

        return productMono.flatMap(p -> {
            if (p.getCreateAt() == null) {
                p.setCreateAt(new Date());
            }
            return productService.save(p);
        })
                //.flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
                //.contentType(MediaType.APPLICATION_JSON)
                //.body(productService.update(p,id),Product.class));
                .flatMap(p -> productService.update(p,id))
        .flatMap(p -> ServerResponse.created(URI.create("/api/client/".concat(id)))
        .contentType(MediaType.APPLICATION_JSON)
        .syncBody(p));

    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");

        return productService.delete(id).then(ServerResponse.noContent().build()
                .onErrorResume(ex -> {
                    WebClientResponseException error = (WebClientResponseException) ex;
                    if (error.getStatusCode() == HttpStatus.BAD_REQUEST) {
                        return ServerResponse.notFound().build();
                    }
                    return Mono.error(error);
                }));
    }

    public Mono<ServerResponse> upload(ServerRequest request) {
        String id = request.pathVariable("id");

        return errorHandler(
                        request.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                        .cast(FilePart.class).flatMap(file -> productService.upload(file,id)).flatMap(p ->
                            ServerResponse.created(URI.create("/api/client".concat(p.getId())))
                            .contentType(MediaType.APPLICATION_JSON)
                            .syncBody(p)
                        )
        );
    }

    private Mono<ServerResponse> errorHandler(Mono<ServerResponse> response) {

        return response.onErrorResume(ex -> {
            WebClientResponseException error = (WebClientResponseException) ex;
            if (error.getStatusCode() == HttpStatus.BAD_REQUEST) {
                //return ServerResponse.notFound().build();
                Map<String, Object> body = new HashMap<>();
                body.put("error", "No existe el producto".concat(ex.getMessage()));
                body.put("TimeStamp",new Date());
                return ServerResponse.status(HttpStatus.NOT_FOUND).syncBody(body);
            }
            return Mono.error(error);
        });
    }

}

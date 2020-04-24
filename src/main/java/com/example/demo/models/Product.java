package com.example.demo.models;

import jdk.jfr.DataAmount;
import lombok.Data;

import java.util.Date;

@Data
public class Product {
    private String id;
    private String name;
    private Double price;
    private Date createAt;
    private String foto;
    private Category category;
}

package com.example.salesinfo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name ="SALES_INFO")
@Data
public class SalesInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    int id;
    private String Product;
    private String Seller;
    private String SellerId;
    private String Price;
    private String City;
    private String Category;

}

package com.br.comunicacao.productapi.modules.product.model;

import com.br.comunicacao.productapi.modules.product.dto.ProductRequest;
import com.br.comunicacao.productapi.modules.supplier.model.Supplier;
import com.br.comunicacao.productapi.modules.category.model.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name="PRODUCT")
@AllArgsConstructor
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name="NAME", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "FK_SUPPLIER", nullable = false)
    private Supplier supplier;

    @ManyToOne
    @JoinColumn(name = "FK_CATEGORY", nullable = false)
    private Category category;

    @Column(name="QUANTITY_AVAILABLE", nullable = false)
    private Integer quantityAvailable;

    @Column(name="CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist(){
        createdAt = LocalDateTime.now();
    }

    public static Product of(ProductRequest request,
                                  Category category,
                                  Supplier supplier) {

        return Product
                .builder()
                .name(request.getName())
                .quantityAvailable(request.getQuantityAvailable())
                .category(category)
                .supplier(supplier).build();
    }

    public void updateStock(Integer quantity){
        this.quantityAvailable = this.quantityAvailable - quantity;
    }

}

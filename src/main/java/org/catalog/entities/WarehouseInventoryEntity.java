package org.catalog.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseInventoryEntity {
    @Id
    private Long id;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 50, nullable = false)
    private String location;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "product_id")
    private ProductEntity productEntity;

}

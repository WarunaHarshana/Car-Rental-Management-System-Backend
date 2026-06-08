package edu.waruna.carrental.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cars")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private String model;

    @Column(name = "fuel_type", nullable = false)
    private String fuelType;

    @Column(name = "seating_capacity", nullable = false)
    private Integer seatingCapacity;

    @Column(name = "daily_price", nullable = false)
    private Double dailyPrice;

    @Column(nullable = false)
    private String status; // AVAILABLE, RENTED, MAINTENANCE

    @Column(name = "image_url")
    private String imageUrl;

}

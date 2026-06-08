package edu.waruna.carrental.controller;


import edu.waruna.carrental.entity.Car;
import edu.waruna.carrental.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    @Autowired
    private CarRepository carRepository;

    @GetMapping
    public List<Car> getAllCars(){
        return carRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return carRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Car createCar(@RequestBody Car car) {
        return carRepository.save(car);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car carDetails) {
        return carRepository.findById(id)
                .map(car -> {
                    car.setBrand(carDetails.getBrand());
                    car.setModel(carDetails.getModel());
                    car.setFuelType(carDetails.getFuelType());
                    car.setSeatingCapacity(carDetails.getSeatingCapacity());
                    car.setDailyPrice(carDetails.getDailyPrice());
                    car.setStatus(carDetails.getStatus());
                    // Preserve imageUrl if not provided in update
                    if (carDetails.getImageUrl() != null) {
                        car.setImageUrl(carDetails.getImageUrl());
                    }
                    Car updatedCar = carRepository.save(car);
                    return ResponseEntity.ok(updatedCar);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (carRepository.existsById(id)) {
            carRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/cars/{id}/photo
     * Accepts a multipart image file, saves it to uploads/cars/,
     * updates the car's imageUrl in the database, and returns the updated car.
     */
    @PostMapping("/{id}/photo")
    public ResponseEntity<?> uploadCarPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            Car car = carRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Car not found with id: " + id));

            String uploadDir = "uploads/cars";
            Files.createDirectories(Paths.get(uploadDir));

            String originalFileName = file.getOriginalFilename();
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String fileName = "car-" + id + "-" + System.currentTimeMillis() + extension;
            Path filePath = Paths.get(uploadDir, fileName);

            Files.write(filePath, file.getBytes());

            String imageUrl = "/uploads/cars/" + fileName;
            car.setImageUrl(imageUrl);
            Car updatedCar = carRepository.save(car);

            return ResponseEntity.ok(updatedCar);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Could not upload image: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }
}

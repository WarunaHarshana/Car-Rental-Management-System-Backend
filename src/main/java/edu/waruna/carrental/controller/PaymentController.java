package edu.waruna.carrental.controller;

import edu.waruna.carrental.entity.Payment;
import edu.waruna.carrental.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Checkout endpoint: creates a Payment and marks booking PAID
    @PostMapping("/checkout/{bookingId}")
    public ResponseEntity<?> checkout(@PathVariable Long bookingId, @RequestBody Map<String, Object> body) {
        Double amount = null;
        String method = "UNKNOWN";
        if (body != null) {
            if (body.get("amount") instanceof Number) {
                amount = ((Number) body.get("amount")).doubleValue();
            }
            if (body.get("method") != null) {
                method = body.get("method").toString();
            }
        }

        if (amount == null) {
            return ResponseEntity.badRequest().body("Missing amount");
        }

        try {
            Payment payment = paymentService.createPaymentForBooking(bookingId, amount, method);
            return ResponseEntity.ok(payment);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Payment processing failed");
        }
    }

    // All payments
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    // Payment history for one booking
    @GetMapping("/booking/{bookingId}")
    public List<Payment> getByBooking(@PathVariable Long bookingId) {
        return paymentService.getPaymentsForBooking(bookingId);
    }
}

package edu.waruna.carrental.service;

import edu.waruna.carrental.entity.Booking;
import edu.waruna.carrental.entity.Payment;
import edu.waruna.carrental.repository.BookingRepository;
import edu.waruna.carrental.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public Payment createPaymentForBooking(Long bookingId, Double amount, String method) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found");
        }

        Booking booking = bookingOpt.get();

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setStatus("SUCCESS");
        payment.setCreatedAt(LocalDateTime.now());

        // Save payment
        Payment saved = paymentRepository.save(payment);

        // Update booking payment status
        booking.setPaymentStatus("PAID");
        bookingRepository.save(booking);

        return saved;
    }

    // All payments (used by admin and the revenue report later)
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Payment history for a single booking
    public List<Payment> getPaymentsForBooking(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId);
    }
}

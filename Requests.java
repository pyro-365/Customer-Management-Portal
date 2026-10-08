package com.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

/** Everything the browser can send to the server. */
public final class Requests {
    private Requests() {}

    public record Register(
            @NotBlank(message = "Enter your name.") String name,
            @NotBlank(message = "Enter your email address.") @Email(message = "Enter a valid email address.") String email,
            @NotBlank(message = "Enter your phone number.") String phone,
            @NotBlank(message = "Enter a password.") @Size(min = 6, message = "Password must be at least 6 characters.") String password) {}

    public record Login(
            @NotBlank(message = "Enter your email or phone number.") String identifier,
            @NotBlank(message = "Enter your password.") String password) {}

    public record ProfileUpdate(
            @NotBlank(message = "Enter your name.") String name,
            @NotBlank(message = "Enter your email address.") @Email(message = "Enter a valid email address.") String email,
            @NotBlank(message = "Enter your phone number.") String phone,
            String address, Double latitude, Double longitude) {}

    public record PasswordChange(
            @NotBlank(message = "Enter your current password.") String current,
            @NotBlank(message = "Enter a new password.") @Size(min = 6, message = "New password must be at least 6 characters.") String next) {}

    public record CartLine(@NotNull(message = "Missing product.") Long productId,
                           @Min(value = 1, message = "Quantity must be at least 1.") int quantity) {}

    public record Checkout(@NotEmpty(message = "Your cart is empty.") List<@Valid CartLine> items) {}

    public record Turn(String role, String text) {}

    public record Chat(@NotBlank(message = "Type a question first.") String message, List<Turn> history) {}

    public record Geocode(@NotBlank(message = "Enter an address to verify.") String address) {}
}

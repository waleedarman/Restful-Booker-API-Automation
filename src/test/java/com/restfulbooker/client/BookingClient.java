package com.restfulbooker.client;

import com.restfulbooker.config.ConfigReader;
import com.restfulbooker.model.Booking;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;

public class BookingClient {
    private final String baseUrl;
    private final AuthClient authClient;

    public BookingClient() {
        this.baseUrl = ConfigReader.getRequiredProperty("base.url");
        this.authClient = new AuthClient();
    }

    public Response createBooking(Booking booking) {
        return createBookingRequest(booking);
    }

    public Response createBooking(Map<String, ?> payload) {
        return createBookingRequest(payload);
    }

    private Response createBookingRequest(Object body) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(body)
                .when()
                .post("/booking");
    }

    public Response getBooking(int bookingId) {
        return given()
                .baseUri(baseUrl)
                .when()
                .get("/booking/{id}", bookingId);
    }

    public Response updateBooking(int bookingId, Booking booking) {
        return updateBookingWithToken(bookingId, booking, authClient.getValidToken());
    }

    public Response updateBookingWithoutAuthentication(int bookingId, Booking booking) {
        return updateBookingRequest(bookingId, booking, null);
    }

    public Response updateBookingWithToken(int bookingId, Booking booking, String token) {
        return updateBookingRequest(bookingId, booking, token);
    }

    private Response updateBookingRequest(int bookingId, Booking booking, String token) {
        RequestSpecification request = given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(booking);
        if (token != null) {
            request.header("Cookie", "token=" + token);
        }
        return request.when().put("/booking/{id}", bookingId);
    }

    public Response partiallyUpdateBooking(int bookingId, Map<String, ?> changes) {
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .header("Cookie", "token=" + authClient.getValidToken())
                .body(changes)
                .when()
                .patch("/booking/{id}", bookingId);
    }

    public Response deleteBooking(int bookingId) {
        return deleteBookingWithToken(bookingId, authClient.getValidToken());
    }

    public Response deleteBookingWithoutAuthentication(int bookingId) {
        return deleteBookingRequest(bookingId, null);
    }

    public Response deleteBookingWithToken(int bookingId, String token) {
        return deleteBookingRequest(bookingId, token);
    }

    private Response deleteBookingRequest(int bookingId, String token) {
        RequestSpecification request = given().baseUri(baseUrl);
        if (token != null) {
            request.header("Cookie", "token=" + token);
        }
        return request.when().delete("/booking/{id}", bookingId);
    }
}

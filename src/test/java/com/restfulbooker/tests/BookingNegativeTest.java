package com.restfulbooker.tests;

import com.restfulbooker.client.BookingClient;
import com.restfulbooker.model.Booking;
import com.restfulbooker.model.BookingDates;
import io.restassured.response.Response;
import org.testng.asserts.SoftAssert;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class BookingNegativeTest {
    private static final long MAX_RESPONSE_TIME_MILLIS = 2000;
    private static final String INVALID_TOKEN = "invalid-token-for-negative-test";

    private final BookingClient bookingClient = new BookingClient();

    @Test(description = "PUT without a token is rejected and leaves the booking unchanged")
    public void putWithoutAuthenticationIsRejected() {
        int bookingId = 0;
        boolean deletionConfirmed = false;
        SoftAssert assertions = new SoftAssert();
        try {
            Booking originalBooking = newBooking("NoTokenPut", 310);
            Response createResponse = createTemporaryBooking(originalBooking, "PUT without auth setup");
            bookingId = extractBookingId(createResponse);
            assertStatusAndTime(createResponse, 200, "PUT without auth setup create booking",
                    assertions);
            assertTrue(bookingId > 0, "PUT without auth setup should return a positive booking ID");

            Response rejected = bookingClient.updateBookingWithoutAuthentication(
                    bookingId, newBooking("RejectedPut", 999));
            logResponse("PUT without token", rejected);
            assertStatusAndTime(rejected, 403, "PUT without token", assertions);

            Booking bookingAfterRejectedPut = getBookingAndVerifyStatus(
                    bookingId, 200, "Verify PUT without token", assertions);
            if (bookingAfterRejectedPut != null) {
                assertBookingEquals(bookingAfterRejectedPut, originalBooking,
                        "Booking after rejected PUT without token");
            }
        } finally {
            deletionConfirmed = cleanupBooking(bookingId, deletionConfirmed, "PUT without token",
                    assertions);
            logCleanup("PUT without token", bookingId, deletionConfirmed);
        }
        assertions.assertAll("PUT without token response checks failed");
    }

    @Test(description = "PUT with an invalid token is rejected and leaves the booking unchanged")
    public void putWithInvalidAuthenticationIsRejected() {
        int bookingId = 0;
        boolean deletionConfirmed = false;
        SoftAssert assertions = new SoftAssert();
        try {
            Booking originalBooking = newBooking("BadTokenPut", 320);
            Response createResponse = createTemporaryBooking(originalBooking, "PUT invalid-token setup");
            bookingId = extractBookingId(createResponse);
            assertStatusAndTime(createResponse, 200, "PUT invalid-token setup create booking",
                    assertions);
            assertTrue(bookingId > 0, "PUT invalid-token setup should return a positive booking ID");

            Response rejected = bookingClient.updateBookingWithToken(
                    bookingId, newBooking("RejectedPut", 999), INVALID_TOKEN);
            logResponse("PUT with invalid token", rejected);
            assertStatusAndTime(rejected, 403, "PUT with invalid token", assertions);

            Booking bookingAfterRejectedPut = getBookingAndVerifyStatus(
                    bookingId, 200, "Verify PUT with invalid token", assertions);
            if (bookingAfterRejectedPut != null) {
                assertBookingEquals(bookingAfterRejectedPut, originalBooking,
                        "Booking after rejected PUT with invalid token");
            }
        } finally {
            deletionConfirmed = cleanupBooking(bookingId, deletionConfirmed, "PUT invalid-token",
                    assertions);
            logCleanup("PUT with invalid token", bookingId, deletionConfirmed);
        }
        assertions.assertAll("PUT with invalid token response checks failed");
    }

    @Test(description = "DELETE without a token is rejected and leaves the booking available")
    public void deleteWithoutAuthenticationIsRejected() {
        int bookingId = 0;
        boolean deletionConfirmed = false;
        SoftAssert assertions = new SoftAssert();
        try {
            Booking originalBooking = newBooking("NoTokenDelete", 330);
            Response createResponse = createTemporaryBooking(originalBooking, "DELETE without auth setup");
            bookingId = extractBookingId(createResponse);
            assertStatusAndTime(createResponse, 200, "DELETE without auth setup create booking",
                    assertions);
            assertTrue(bookingId > 0, "DELETE without auth setup should return a positive booking ID");

            Response rejected = bookingClient.deleteBookingWithoutAuthentication(bookingId);
            logResponse("DELETE without token", rejected);
            assertStatusAndTime(rejected, 403, "DELETE without token", assertions);

            Booking bookingAfterRejectedDelete = getBookingAndVerifyStatus(
                    bookingId, 200, "Verify DELETE without token", assertions);
            if (bookingAfterRejectedDelete != null) {
                assertBookingEquals(bookingAfterRejectedDelete, originalBooking,
                        "Booking after rejected DELETE without token");
            }
        } finally {
            deletionConfirmed = cleanupBooking(bookingId, deletionConfirmed, "DELETE without token",
                    assertions);
            logCleanup("DELETE without token", bookingId, deletionConfirmed);
        }
        assertions.assertAll("DELETE without token response checks failed");
    }

    @Test(description = "DELETE with an invalid token is rejected and leaves the booking available")
    public void deleteWithInvalidAuthenticationIsRejected() {
        int bookingId = 0;
        boolean deletionConfirmed = false;
        SoftAssert assertions = new SoftAssert();
        try {
            Booking originalBooking = newBooking("BadTokenDelete", 340);
            Response createResponse = createTemporaryBooking(originalBooking, "DELETE invalid-token setup");
            bookingId = extractBookingId(createResponse);
            assertStatusAndTime(createResponse, 200, "DELETE invalid-token setup create booking",
                    assertions);
            assertTrue(bookingId > 0, "DELETE invalid-token setup should return a positive booking ID");

            Response rejected = bookingClient.deleteBookingWithToken(bookingId, INVALID_TOKEN);
            logResponse("DELETE with invalid token", rejected);
            assertStatusAndTime(rejected, 403, "DELETE with invalid token", assertions);

            Booking bookingAfterRejectedDelete = getBookingAndVerifyStatus(
                    bookingId, 200, "Verify DELETE with invalid token", assertions);
            if (bookingAfterRejectedDelete != null) {
                assertBookingEquals(bookingAfterRejectedDelete, originalBooking,
                        "Booking after rejected DELETE with invalid token");
            }
        } finally {
            deletionConfirmed = cleanupBooking(bookingId, deletionConfirmed, "DELETE invalid-token",
                    assertions);
            logCleanup("DELETE with invalid token", bookingId, deletionConfirmed);
        }
        assertions.assertAll("DELETE with invalid token response checks failed");
    }

    @Test(description = "GET for a booking deleted during this test returns 404")
    public void getDeletedBookingReturnsNotFound() {
        int bookingId = 0;
        boolean deletionConfirmed = false;
        SoftAssert assertions = new SoftAssert();
        try {
            Response createResponse = createTemporaryBooking(
                    newBooking("DeletedBooking", 350), "GET nonexistent setup");
            bookingId = extractBookingId(createResponse);
            assertStatusAndTime(createResponse, 200, "GET nonexistent setup create booking",
                    assertions);
            assertTrue(bookingId > 0, "GET nonexistent setup should return a positive booking ID");
            Response deleteResponse = bookingClient.deleteBooking(bookingId);
            logResponse("DELETE before nonexistent GET", deleteResponse);
            assertStatusAndTime(deleteResponse, 201, "Delete booking before nonexistent GET",
                    assertions);

            Response getDeletedBooking = bookingClient.getBooking(bookingId);
            logResponse("GET deleted booking", getDeletedBooking);
            assertStatusAndTime(getDeletedBooking, 404, "GET deleted booking", assertions);
            deletionConfirmed = getDeletedBooking.statusCode() == 404;
        } finally {
            deletionConfirmed = cleanupBooking(bookingId, deletionConfirmed, "GET deleted booking",
                    assertions);
            logCleanup("GET deleted booking", bookingId, deletionConfirmed);
        }
        assertions.assertAll("GET deleted booking response checks failed");
    }

    @Test(description = "POST with missing required fields returns HTTP 400")
    public void postWithMissingRequiredFieldsIsRejected() {
        Response response = bookingClient.createBooking(Map.of("firstname", "Incomplete"));
        logResponse("POST with missing required fields", response);
        SoftAssert assertions = new SoftAssert();
        assertStatusAndTime(response, 400, "POST with missing required fields", assertions);
        assertions.assertAll("POST with missing required fields response checks failed");
    }

    private Response createTemporaryBooking(Booking booking, String context) {
        Response response = bookingClient.createBooking(booking);
        logResponse(context + " POST /booking", response);
        return response;
    }

    private int extractBookingId(Response response) {
        try {
            Object value = response.jsonPath().get("bookingid");
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        } catch (RuntimeException ignored) {
        }
        return 0;
    }

    private Booking getBookingAndVerifyStatus(int bookingId, int expectedStatus, String context,
                                              SoftAssert assertions) {
        Response response = bookingClient.getBooking(bookingId);
        logResponse(context + " GET /booking/{id}", response);
        assertStatusAndTime(response, expectedStatus, context, assertions);
        if (response.statusCode() != expectedStatus) {
            return null;
        }
        return response.as(Booking.class);
    }

    private boolean cleanupBooking(int bookingId, boolean deletionConfirmed, String context,
                                   SoftAssert assertions) {
        if (bookingId <= 0 || deletionConfirmed) {
            return deletionConfirmed;
        }
        try {
            Response check = bookingClient.getBooking(bookingId);
            logResponse(context + " cleanup GET", check);
            assertions.assertTrue(check.getTime() < MAX_RESPONSE_TIME_MILLIS,
                    context + " cleanup GET should complete in under 2000 ms");
            assertions.assertTrue(check.statusCode() == 200 || check.statusCode() == 404,
                    context + " cleanup GET should return HTTP 200 or 404");
            if (check.statusCode() == 404) {
                return true;
            }
            if (check.statusCode() != 200) {
                System.err.printf("Cleanup GET returned an unexpected status for %s%n", context);
                return false;
            }

            Response cleanup = bookingClient.deleteBooking(bookingId);
            logResponse(context + " cleanup DELETE", cleanup);
            if (cleanup.getTime() >= MAX_RESPONSE_TIME_MILLIS || cleanup.statusCode() != 201) {
                System.err.printf("Cleanup DELETE failed for %s; see response details above%n", context);
            }
            assertions.assertTrue(cleanup.getTime() < MAX_RESPONSE_TIME_MILLIS,
                    context + " cleanup DELETE should complete in under 2000 ms");
            assertions.assertEquals(cleanup.statusCode(), 201,
                    context + " cleanup DELETE should return HTTP 201");
            return cleanup.statusCode() == 201;
        } catch (RuntimeException cleanupFailure) {
            System.err.printf("Cleanup failed for %s (%s)%n", context,
                    cleanupFailure.getClass().getSimpleName());
            assertions.fail(context + " cleanup request failed");
            return false;
        }
    }

    private void logCleanup(String context, int bookingId, boolean deletionConfirmed) {
        if (bookingId <= 0) {
            System.out.printf("Cleanup for %s: no booking was created%n", context);
        } else if (deletionConfirmed) {
            System.out.printf("Cleanup for %s: booking removal confirmed%n", context);
        } else {
            System.out.printf("Cleanup for %s: booking removal was not confirmed%n", context);
        }
    }

    private Booking newBooking(String firstname, int totalPrice) {
        return new Booking(firstname, "NegativeTest", totalPrice, false,
                new BookingDates("2027-06-10", "2027-06-14"), "Test cleanup");
    }

    private void assertStatusAndTime(Response response, int expectedStatus, String context,
                                     SoftAssert assertions) {
        assertions.assertEquals(response.statusCode(), expectedStatus,
                context + " should return HTTP " + expectedStatus);
        assertions.assertTrue(response.getTime() < MAX_RESPONSE_TIME_MILLIS,
                context + " should complete in under 2000 ms, got " + response.getTime() + " ms");
    }

    private void logResponse(String operation, Response response) {
        System.out.printf("%s: status=%d, responseTimeMs=%d%n",
                operation, response.statusCode(), response.getTime());
    }

    private void assertBookingEquals(Booking actual, Booking expected, String context) {
        assertNotNull(actual, context + " should not be null");
        assertEquals(actual.getFirstname(), expected.getFirstname(), context + ": firstname");
        assertEquals(actual.getLastname(), expected.getLastname(), context + ": lastname");
        assertEquals(actual.getTotalprice(), expected.getTotalprice(), context + ": totalprice");
        assertEquals(actual.isDepositpaid(), expected.isDepositpaid(), context + ": depositpaid");
        assertNotNull(actual.getBookingdates(), context + ": bookingdates should not be null");
        assertEquals(actual.getBookingdates().getCheckin(), expected.getBookingdates().getCheckin(),
                context + ": bookingdates.checkin");
        assertEquals(actual.getBookingdates().getCheckout(), expected.getBookingdates().getCheckout(),
                context + ": bookingdates.checkout");
        assertEquals(actual.getAdditionalneeds(), expected.getAdditionalneeds(),
                context + ": additionalneeds");
    }
}

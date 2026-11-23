package com.restfulbooker.tests;

import com.restfulbooker.client.BookingClient;
import com.restfulbooker.model.Booking;
import com.restfulbooker.model.BookingDates;
import com.restfulbooker.model.BookingResponse;
import io.restassured.response.Response;
import org.testng.asserts.SoftAssert;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class BookingCrudTest {
    private static final long MAX_RESPONSE_TIME_MILLIS = 2000;
    private final BookingClient bookingClient = new BookingClient();

    @Test(description = "Create, read, update, partially update, and delete a booking")
    public void testBookingCrud() {
        int bookingId = 0;
        boolean deletionConfirmed = false;
        SoftAssert cleanupAssertions = new SoftAssert();

        try {
            Booking booking = new Booking(
                    "Morgan", "Taylor", 275, true,
                    new BookingDates("2027-04-12", "2027-04-16"), "Breakfast");

            Response createResponse = bookingClient.createBooking(booking);
            logResponse("CREATE", createResponse);
            bookingId = extractBookingId(createResponse);
            assertStatusAndTime(createResponse, 200, "CREATE");
            validateSchema(createResponse, "schemas/booking-response-schema.json");
            BookingResponse created = createResponse.as(BookingResponse.class);
            assertNotNull(created.getBooking(), "CREATE response should contain a booking");
            assertTrue(bookingId > 0, "CREATE should return a positive booking ID");
            assertBookingEquals(created.getBooking(), booking, "CREATE response");

            Response getResponse = bookingClient.getBooking(bookingId);
            logResponse("GET", getResponse);
            assertStatusAndTime(getResponse, 200, "GET");
            validateSchema(getResponse, "schemas/booking-schema.json");
            assertBookingEquals(getResponse.as(Booking.class), booking, "GET response");

            Booking updatedBooking = new Booking(
                    "Riley", "Jordan", 410, false,
                    new BookingDates("2027-05-20", "2027-05-25"), "Airport transfer");
            Response putResponse = bookingClient.updateBooking(bookingId, updatedBooking);
            logResponse("PUT", putResponse);
            assertStatusAndTime(putResponse, 200, "PUT");
            validateSchema(putResponse, "schemas/booking-schema.json");
            assertBookingEquals(putResponse.as(Booking.class), updatedBooking, "PUT response");

            Response getAfterPut = bookingClient.getBooking(bookingId);
            logResponse("GET after PUT", getAfterPut);
            assertStatusAndTime(getAfterPut, 200, "GET after PUT");
            validateSchema(getAfterPut, "schemas/booking-schema.json");
            assertBookingEquals(getAfterPut.as(Booking.class), updatedBooking, "GET after PUT response");

            String patchedFirstName = "Casey";
            Response patchResponse = bookingClient.partiallyUpdateBooking(
                    bookingId, Map.of("firstname", patchedFirstName));
            logResponse("PATCH", patchResponse);
            assertStatusAndTime(patchResponse, 200, "PATCH");
            validateSchema(patchResponse, "schemas/booking-schema.json");
            Booking patchedBooking = new Booking(patchedFirstName, updatedBooking.getLastname(),
                    updatedBooking.getTotalprice(), updatedBooking.isDepositpaid(),
                    updatedBooking.getBookingdates(), updatedBooking.getAdditionalneeds());
            assertBookingEquals(patchResponse.as(Booking.class), patchedBooking, "PATCH response");

            Response getAfterPatch = bookingClient.getBooking(bookingId);
            logResponse("GET after PATCH", getAfterPatch);
            assertStatusAndTime(getAfterPatch, 200, "GET after PATCH");
            validateSchema(getAfterPatch, "schemas/booking-schema.json");
            assertBookingEquals(getAfterPatch.as(Booking.class), patchedBooking,
                    "GET after PATCH response");

            Response deleteResponse = bookingClient.deleteBooking(bookingId);
            logResponse("DELETE", deleteResponse);
            assertStatusAndTime(deleteResponse, 201, "DELETE");

            Response getAfterDelete = bookingClient.getBooking(bookingId);
            logResponse("GET after DELETE", getAfterDelete);
            deletionConfirmed = getAfterDelete.statusCode() == 404;
            assertStatusAndTime(getAfterDelete, 404, "GET after DELETE");
        } finally {
            if (bookingId > 0 && !deletionConfirmed) {
                cleanupBooking(bookingId, cleanupAssertions);
            }
        }
        cleanupAssertions.assertAll("Cleanup response checks failed");
    }

    private void cleanupBooking(int bookingId, SoftAssert assertions) {
        try {
            Response check = bookingClient.getBooking(bookingId);
            logResponse("CLEANUP GET", check);
            assertions.assertTrue(check.getTime() < MAX_RESPONSE_TIME_MILLIS,
                    "Cleanup GET should take under 2000 ms");
            assertions.assertTrue(check.statusCode() == 200 || check.statusCode() == 404,
                    "Cleanup GET should return HTTP 200 or 404");
            if (check.statusCode() == 404) {
                return;
            }
            if (check.statusCode() != 200) {
                System.err.println("Cleanup GET returned an unexpected status");
                return;
            }

            Response deleted = bookingClient.deleteBooking(bookingId);
            logResponse("CLEANUP DELETE", deleted);
            if (deleted.getTime() >= MAX_RESPONSE_TIME_MILLIS || deleted.statusCode() != 201) {
                System.err.println("Cleanup DELETE failed; see response details above");
            }
            assertions.assertTrue(deleted.getTime() < MAX_RESPONSE_TIME_MILLIS,
                    "Cleanup DELETE should take under 2000 ms");
            assertions.assertEquals(deleted.statusCode(), 201,
                    "Cleanup DELETE should return HTTP 201");
        } catch (RuntimeException cleanupFailure) {
            System.err.printf("Best-effort cleanup failed (%s)%n",
                    cleanupFailure.getClass().getSimpleName());
            assertions.fail("Best-effort cleanup request failed");
        }
    }

    private void assertStatusAndTime(Response response, int expectedStatus, String operation) {
        SoftAssert assertions = new SoftAssert();
        assertions.assertEquals(response.statusCode(), expectedStatus,
                operation + " returned an unexpected HTTP status");
        assertions.assertTrue(response.getTime() < MAX_RESPONSE_TIME_MILLIS,
                operation + " should complete in under 2000 ms, got " + response.getTime() + " ms");
        assertions.assertAll(operation + " response checks failed");
    }

    private void logResponse(String operation, Response response) {
        System.out.printf("%s: status=%d, responseTimeMs=%d%n",
                operation, response.statusCode(), response.getTime());
    }

    private void validateSchema(Response response, String schemaPath) {
        response.then()
                .assertThat()
                .body(matchesJsonSchemaInClasspath(schemaPath));
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

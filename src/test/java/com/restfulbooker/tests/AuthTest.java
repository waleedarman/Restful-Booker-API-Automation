package com.restfulbooker.tests;

import com.restfulbooker.client.AuthClient;
import com.restfulbooker.model.AuthResponse;
import io.restassured.response.Response;
import org.testng.asserts.SoftAssert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class AuthTest {
    private static final long MAX_RESPONSE_TIME_MILLIS = 2000;
    private final AuthClient authClient = new AuthClient();

    @Test(description = "Valid configured credentials return a non-blank token")
    public void validCredentialsReturnToken() {
        Response response = authClient.authenticate();
        long responseTime = response.getTime();
        System.out.printf("Valid authentication: status=%d, responseTimeMs=%d%n",
                response.statusCode(), responseTime);

        SoftAssert assertions = new SoftAssert();
        assertions.assertEquals(response.statusCode(), 200, "Expected HTTP 200 for valid credentials");
        assertions.assertTrue(responseTime < MAX_RESPONSE_TIME_MILLIS,
                "Expected response time under 2000 ms, got " + responseTime + " ms");
        assertions.assertAll("Valid authentication response checks failed");

        AuthResponse authResponse = response.as(AuthResponse.class);
        assertNotNull(authResponse.getToken(), "Expected a token in the authentication response");
        assertTrue(!authResponse.getToken().isBlank(), "Expected a non-blank token");
    }

    @Test(description = "Invalid credentials return the documented authentication failure")
    public void invalidCredentialsAreRejected() {
        Response response = authClient.authenticate("invalid-user", "invalid-password");
        long responseTime = response.getTime();
        System.out.printf("Invalid authentication: status=%d, responseTimeMs=%d%n",
                response.statusCode(), responseTime);

        SoftAssert assertions = new SoftAssert();
        assertions.assertEquals(response.statusCode(), 200,
                "Expected documented HTTP 200 response for invalid credentials");
        assertions.assertTrue(responseTime < MAX_RESPONSE_TIME_MILLIS,
                "Expected response time under 2000 ms, got " + responseTime + " ms");
        assertions.assertAll("Invalid authentication response checks failed");
        assertEquals(response.jsonPath().getString("reason"), "Bad credentials",
                "Expected documented authentication failure message");

        String token = response.jsonPath().getString("token");
        assertTrue(token == null || token.isBlank(),
                "Invalid credentials must not return a valid token");
    }
}

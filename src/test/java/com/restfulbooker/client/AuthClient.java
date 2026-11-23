package com.restfulbooker.client;

import com.restfulbooker.config.ConfigReader;
import com.restfulbooker.model.AuthRequest;
import com.restfulbooker.model.AuthResponse;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class AuthClient {
    private static final long MAX_RESPONSE_TIME_MILLIS = 2000;
    private static final String BASE_URL_KEY = "base.url";
    private static final String USERNAME_KEY = "auth.username";
    private static final String PASSWORD_KEY = "auth.password";

    private final String baseUrl;

    public AuthClient() {
        this.baseUrl = ConfigReader.getRequiredProperty(BASE_URL_KEY);
    }

    public Response authenticate() {
        String username = ConfigReader.getRequiredProperty(USERNAME_KEY);
        String password = ConfigReader.getRequiredProperty(PASSWORD_KEY);
        return authenticate(username, password);
    }

    public Response authenticate(String username, String password) {
        AuthRequest request = new AuthRequest(username, password);
        return given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body(request)
                .when()
                .post("/auth");
    }

    public String getValidToken() {
        Response response = authenticate();
        int statusCode = response.statusCode();
        long responseTime = response.getTime();
        if (statusCode != 200 || responseTime >= MAX_RESPONSE_TIME_MILLIS) {
            throw new IllegalStateException("Authentication request failed: status=" + statusCode
                    + ", responseTimeMs=" + responseTime);
        }
        AuthResponse authResponse = response.as(AuthResponse.class);
        String token = authResponse.getToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Authentication failed: response did not contain a valid token");
        }
        return token;
    }
}

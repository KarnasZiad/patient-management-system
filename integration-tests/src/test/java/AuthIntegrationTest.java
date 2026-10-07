import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

public class AuthIntegrationTest extends BaseIntegrationTest {

  @Test
  public void shouldReturnOKWithValidToken() {
    given()
        .contentType("application/json")
        .body(loginPayload(TEST_USER_EMAIL, TEST_USER_PASSWORD))
        .when()
        .post("/auth/login")
        .then()
        .statusCode(200)
        .body("token", notNullValue());
  }

  @Test
  public void shouldReturnUnauthorizedOnInvalidLogin() {
    given()
        .contentType("application/json")
        .body(loginPayload("invalid_user@test.com", "wrongpassword"))
        .when()
        .post("/auth/login")
        .then()
        .statusCode(401);
  }

  @Test
  public void shouldReturnBadRequestOnMalformedLogin() {
    given()
        .contentType("application/json")
        .body(loginPayload("not-an-email", "short"))
        .when()
        .post("/auth/login")
        .then()
        .statusCode(400)
        .body("email", notNullValue())
        .body("password", notNullValue());
  }
}

import static io.restassured.RestAssured.given;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

/**
 * Shared setup for integration tests. The target and credentials can be
 * overridden without touching the code, e.g.
 * {@code mvn test -Dapi.base-url=http://lb-xxx.elb.localhost.localstack.cloud:4004}
 */
public abstract class BaseIntegrationTest {

  protected static final String TEST_USER_EMAIL =
      System.getProperty("test.user.email", "testuser@test.com");
  protected static final String TEST_USER_PASSWORD =
      System.getProperty("test.user.password", "password123");

  @BeforeAll
  static void configureBaseUri() {
    RestAssured.baseURI =
        System.getProperty("api.base-url", "http://localhost:4004");
  }

  protected static String loginPayload(String email, String password) {
    return """
          {
            "email": "%s",
            "password": "%s"
          }
        """.formatted(email, password);
  }

  protected static String obtainToken() {
    return given()
        .contentType("application/json")
        .body(loginPayload(TEST_USER_EMAIL, TEST_USER_PASSWORD))
        .when()
        .post("/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .get("token");
  }
}

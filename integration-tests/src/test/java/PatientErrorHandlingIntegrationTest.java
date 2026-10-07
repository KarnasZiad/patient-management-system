import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PatientErrorHandlingIntegrationTest extends BaseIntegrationTest {

  private static String token;

  @BeforeAll
  static void setUp() {
    token = obtainToken();
  }

  @Test
  public void shouldReturnNotFoundWhenDeletingUnknownPatient() {
    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .delete("/api/patients/" + UUID.randomUUID())
        .then()
        .statusCode(404);
  }

  @Test
  public void shouldReturnNotFoundWhenUpdatingUnknownPatient() {
    String payload = """
          {
            "name": "Jane Doe",
            "email": "jane.unknown@example.com",
            "address": "1 Main Street",
            "dateOfBirth": "1990-01-01"
          }
        """;

    given()
        .header("Authorization", "Bearer " + token)
        .contentType("application/json")
        .body(payload)
        .when()
        .put("/api/patients/" + UUID.randomUUID())
        .then()
        .statusCode(404);
  }

  @Test
  public void shouldReturnBadRequestOnInvalidDate() {
    String payload = """
          {
            "name": "Bad Date",
            "email": "bad.date.%s@example.com",
            "address": "1 Main Street",
            "dateOfBirth": "not-a-date",
            "registeredDate": "2024-01-01"
          }
        """.formatted(UUID.randomUUID());

    given()
        .header("Authorization", "Bearer " + token)
        .contentType("application/json")
        .body(payload)
        .when()
        .post("/api/patients")
        .then()
        .statusCode(400)
        .body("message", containsString("not-a-date"));
  }

  @Test
  public void shouldReturnUnauthorizedWithoutToken() {
    given()
        .when()
        .get("/api/patients")
        .then()
        .statusCode(401);
  }
}

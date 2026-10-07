import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

public class PatientIntegrationTest extends BaseIntegrationTest {

  @Test
  public void shouldReturnPatientsWithValidToken() {
    String token = obtainToken();

    given()
        .header("Authorization", "Bearer " + token)
        .when()
        .get("/api/patients")
        .then()
        .statusCode(200)
        .body("$", notNullValue());
  }
}

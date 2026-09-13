package NovaAutomation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class NovaAPItest {


    private static final String BASE_URL = "https://novatools.org/";
    private String authToken;
    private String createdTaskId;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.config = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam("http.connection.timeout", 10000)
                .setParam("http.socket.timeout", 10000)
                .setParam("http.connection-manager.timeout", 10000));
    }
//mvn clean test -DNOVA_API_Email="abiduddina69@gmail.com" -DNOVA_API_PASSWORD="Password123!"
    // 1. POST /login - Authenticate and retrieve token
    @Test(priority = 1)
    public void testUserAuthentication() {
        String email = getConfigValue("NOVA_API_EMAIL");
        String password = getConfigValue("NOVA_API_PASSWORD");
        Assert.assertTrue(email != null && !email.isBlank(),
            "NOVA_API_EMAIL is not configured. Set it with 'set NOVA_API_EMAIL=...' or pass -DNOVA_API_EMAIL=... to Maven.");
        Assert.assertTrue(password != null && !password.isBlank(),
            "NOVA_API_PASSWORD is not configured. Set it with 'set NOVA_API_PASSWORD=...' or pass -DNOVA_API_PASSWORD=... to Maven.");

        Map<String, String> credentials = new HashMap<>();
        credentials.put("email", email);
        credentials.put("password", password);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(credentials)
                .post("/auth/login");

        saveApiEvidence("login", response, false);
        assertStatus(response, 200, "Authentication failed");

        authToken = response.jsonPath().getString("token");
        Assert.assertTrue(authToken != null && !authToken.isBlank(), "Token was not generated.");
    }

    private String getConfigValue(String name) {
        String value = System.getenv(name);
        return value != null && !value.isBlank() ? value : System.getProperty(name);
    }

    // 2. POST /tasks - Create a new task
    @Test(priority = 2, dependsOnMethods = {"testUserAuthentication"})
    public void testCreateTask() {
        Assert.assertTrue(authToken != null && !authToken.isBlank(), "Authentication token is not available.");

        Map<String, Object> taskBody = new HashMap<>();
        taskBody.put("title", "Execute Selenium Automation");
        taskBody.put("description", "Complete the UI automation scenarios for evaluation");
        taskBody.put("priority", "High");
        taskBody.put("dueDate", LocalDate.now().plusDays(30).toString());

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(taskBody)
                .post("/tasks");

            saveApiEvidence("create-task", response, true);
        assertStatus(response, 201, "Task creation failed");
        Assert.assertEquals(response.jsonPath().getString("title"), "Execute Selenium Automation");
        
        createdTaskId = response.jsonPath().getString("id");
        Assert.assertNotNull(createdTaskId, "Task ID was not returned.");
    }

    // 3. GET /tasks/{id} - Retrieve the created task details
    @Test(priority = 3, dependsOnMethods = {"testCreateTask"})
    public void testGetTaskById() {
        Assert.assertTrue(authToken != null && !authToken.isBlank(), "Authentication token is not available.");

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .get("/tasks/" + createdTaskId);

            saveApiEvidence("get-task", response, true);
        assertStatus(response, 200, "Task retrieval failed");
        Assert.assertEquals(response.jsonPath().getString("id"), createdTaskId);
    }

    // 4. PUT /tasks/{id} - Update task status to completed
    @Test(priority = 4, dependsOnMethods = {"testCreateTask"})
    public void testUpdateTaskStatus() {
        Assert.assertTrue(authToken != null && !authToken.isBlank(), "Authentication token is not available.");

        Map<String, String> updateBody = new HashMap<>();
        updateBody.put("status", "Completed");

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .contentType(ContentType.JSON)
                .body(updateBody)
                .put("/tasks/" + createdTaskId);

            saveApiEvidence("update-task", response, true);
        assertStatus(response, 200, "Task update failed");
        Assert.assertEquals(response.jsonPath().getString("status"), "Completed");
    }

    // 5. DELETE /tasks/{id} - Delete the task from the system
    @Test(priority = 5, dependsOnMethods = {"testCreateTask"})
    public void testDeleteTask() {
        Assert.assertTrue(authToken != null && !authToken.isBlank(), "Authentication token is not available.");

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + authToken)
                .delete("/tasks/" + createdTaskId);

        // 204 No Content is standard for successful deletions. Change to 200 if the API returns a body.
        saveApiEvidence("delete-task", response, true);
        assertStatus(response, 204, "Task deletion failed");
    }

    private void saveApiEvidence(String name, Response response, boolean includeBody) {
        try {
                Path evidenceDirectory = Path.of(
                    "C:\\Users\\abida\\OneDrive\\Desktop\\Abiduddin Ahmed - QA Assignment NOVA\\Test Evidence\\API Automation");
            Files.createDirectories(evidenceDirectory);

            String evidence = "Status: " + response.getStatusCode() + System.lineSeparator();
            if (includeBody) {
                evidence += "Response:" + System.lineSeparator() + response.asPrettyString();
            } else {
                evidence += "Response body omitted because it may contain an authentication token.";
            }

            Path evidenceFile = evidenceDirectory.resolve(
                    name + "-" + System.currentTimeMillis() + ".txt");
            Files.writeString(evidenceFile, evidence);
            System.out.println("API evidence saved: " + evidenceFile.toAbsolutePath());
        } catch (IOException exception) {
            Assert.fail("Unable to save API evidence: " + exception.getMessage());
        }
    }

    private void assertStatus(Response response, int expectedStatus, String message) {
        Assert.assertEquals(response.getStatusCode(), expectedStatus,
                message + ". Expected status " + expectedStatus + " but received "
                        + response.getStatusCode() + ". Response: " + response.asString());
    }
}


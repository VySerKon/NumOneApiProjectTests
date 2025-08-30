import io.restassured.response.Response;
import models.RegistrationRequest;
import models.UserData;
import models.UserRequest;
import models.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import specs.AllSpecs;

import java.util.List;
import java.util.stream.Stream;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;


public class ApiTest extends ApiTestBase {


    static Stream<Arguments> dataUsers() {
        return Stream.of(
                Arguments.of(7, "Michael"),
                Arguments.of(8, "Lindsay"),
                Arguments.of(9, "Tobias"),
                Arguments.of(10, "Byron"),
                Arguments.of(11, "George"),
                Arguments.of(12, "Rachel")
        );
    }

    @ParameterizedTest
    @MethodSource("dataUsers")
    @DisplayName("Проверка полного соответствия имён и идентификаторов пользователей")
    void findNameForAllUsers(int userId, String hisName) {
        List<UserData> users = step("Получаем список пользователей", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .queryParam("page", 2)
                        .when()
                        .get("/users")
                        .then()
                        .spec(AllSpecs.response200)
                        .extract()
                        .jsonPath()
                        .getList("data", UserData.class)
        );

        UserData foundUser = step("Ищем пользователя по ID: " + userId, () ->
                users.stream()
                        .filter(user -> user.getId().equals(userId))
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Пользователь не найден!"))
        );
        step("Проверяем имя пользователя", () -> assertEquals(hisName, foundUser.getFirstName()));
    }

    @Test
    @DisplayName("Проверка корректного сохранения параметров пользователя при создании")
    void checkUserCreation() {
        UserRequest userRequest = step("Создаём тестового пользователя", () ->
                UserRequest.builder()
                        .name("Ivan")
                        .job("Tester")
                        .build());
        UserResponse response = step("Отправляем POST-запрос", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .body(userRequest)
                        .when()
                        .post("/users")
                        .then()
                        .spec(AllSpecs.response201)
                        .extract()
                        .as(UserResponse.class));
        step("Проверяем ответ", () -> {
            assertEquals("Ivan", response.getName());
            assertEquals("Tester", response.getJob());
            assertNotNull(response.getId());
            assertNotNull(response.getCreatedAt());
        });
    }

    private int userId;

    @BeforeEach
    void setUp() {
        UserRequest userRequest = step("Подготавливаем тестовые данные", () ->
                UserRequest.builder()
                        .name("Ivan")
                        .job("Tester")
                        .build());
        UserResponse response = step("Создаём пользователя", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .body(userRequest)
                        .when()
                        .post("/users")
                        .then()
                        .log().all()
                        .extract().
                        as(UserResponse.class));
        userId = step("Сохраняем ID пользователя", () ->
                Integer.parseInt(response.getId()));
    }

    @Test
    @DisplayName("Обновление пользователя через PUT (с проверкой ответа)")
    void updateUser_PutRequest_ReturnsUpdatedData() {
        UserRequest updatedUser = step("Подготавливаем данные для обновления", () ->
                UserRequest.builder()
                        .name("SuperIvan")
                        .job("AutoTester")
                        .build());
        UserResponse response = step("Отправляем PUT-запрос", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .body(updatedUser)
                        .when()
                        .put("/users/" + userId)
                        .then()
                        .spec(AllSpecs.response200)
                        .extract()
                        .as(UserResponse.class));
        step("Проверяем ответ", () -> {
            assertEquals("SuperIvan", response.getName());
            assertEquals("AutoTester", response.getJob());
            assertNotNull(response.getUpdatedAt());
        });
    }

    @AfterEach
    void tearDown() {
        step("Удаляем пользователя с ID: " + userId, () -> {
            given()
                    .spec(AllSpecs.requestSpec)
                    .when()
                    .delete("/users/" + userId)
                    .then()
                    .spec(AllSpecs.response204);
        });
    }

    @Test
    @DisplayName("Проверка структуры ответа для списка пользователей")
    void checkUsersListStructure() {
        Response response = step("Отправляем GET-запрос", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .queryParam("page", 2)
                        .when()
                        .get("/users")
                        .then()
                        .extract()
                        .response());

        step("Проверяем структуру ответа", () -> {
            assertEquals(Integer.valueOf(2), response.path("page"));
            List<UserData> users = response.jsonPath().getList("data", UserData.class);
            assertFalse(users.isEmpty());
            users.forEach(user -> {
                assertNotNull(user.getId());
                assertNotNull(user.getEmail());
            });
        });
    }

    static Stream<Arguments> registrationData() {
        return Stream.of(
                Arguments.of("eve.holt@reqres.in", "pistol", 200, "id", "token"),
                Arguments.of("invalid@email", "123", 400, "error", "Missing password")
        );
    }

    @ParameterizedTest
    @MethodSource("registrationData")
    @DisplayName("Проверка регистрации с разными наборами данных")
    void testUserRegistration(String email, String password, int statusCode, String expectedField) {
        RegistrationRequest request = RegistrationRequest.builder()
                .email(email)
                .password(password)
                .build();

        Response response = step("Отправляем POST-запрос на регистрацию", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .body(request)
                        .when()
                        .post("/register")
                        .then()
                        .statusCode(statusCode)
                        .extract()
                        .response());

        step("Проверяем наличие поля в ответе", () ->
                assertNotNull(response.path(expectedField)));
    }

    @Test
    @DisplayName("Проверка запроса с задержкой ответа")
    void checkDelayedResponse() {
        List<UserData> users = step("Отправляем GET-запрос с задержкой", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .queryParam("delay", 3)
                        .when()
                        .get("/users")
                        .then()
                        .spec(AllSpecs.response200)
                        .extract()
                        .jsonPath()
                        .getList("data", UserData.class));

        step("Проверяем, что пользователи получены", () ->
                assertFalse(users.isEmpty()));
    }

    @Test
    @DisplayName("Частичное обновление данных пользователя через PATCH")
    void partialUpdateUser_PatchRequest() {
        UserRequest partialUpdate = UserRequest.builder()
                .job("QA Engineer")
                .build();

        UserResponse response = step("Отправляем PATCH-запрос", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .body(partialUpdate)
                        .when()
                        .patch("/users/2")
                        .then()
                        .spec(AllSpecs.response200)
                        .extract()
                        .as(UserResponse.class));

        step("Проверяем, что обновилось только поле 'job'", () -> {
            assertEquals("QA Engineer", response.getJob());
            assertNotNull(response.getUpdatedAt());
        });
    }

    @Test
    @DisplayName("Попытка получить несуществующего пользователя")
    void getNonExistentUser_Returns404() {
        step("Отправляем GET-запрос к несуществующему пользователю", () ->
                given()
                        .spec(AllSpecs.requestSpec)
                        .when()
                        .get("/users/999")
                        .then()
                        .spec(AllSpecs.response404));
    }
}





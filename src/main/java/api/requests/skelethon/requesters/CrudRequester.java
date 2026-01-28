package api.requests.skelethon.requesters;

import api.configs.Config;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;
import api.requests.skelethon.interfaces.GetAllEndpointInterface;
import common.helpers.StepLogger;
import io.qameta.allure.Step;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import static io.restassured.RestAssured.given;

public class CrudRequester extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    private final static String API_VERSION = Config.getProperty("apiVersion");

    public CrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        return StepLogger.log("POST request to " + endpoint.getUrl(), () -> {
            var body = model == null ? "" : model;
            return given()
                    .spec(requestSpecification)
                    .body(body)
                    .post(API_VERSION + endpoint.getUrl())
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("GET запрос на {endpoint} с id {id}")
    public ValidatableResponse get(long id) {
        return StepLogger.log("GET request to " + endpoint.getUrl() + " with id " + id, () -> {
            String url = API_VERSION + endpoint.getUrl().replace("{id}", String.valueOf(id));
            return given()
                    .spec(requestSpecification)
                    .get(url)
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("PUT запрос на {endpoint} с телом {model}")
    public Object update(long id, BaseModel model) {
        return StepLogger.log("PUT request to " + endpoint.getUrl() + " with id " + id, () -> {
            String url = API_VERSION + endpoint.getUrl();
            // Заменяем {id} только если он присутствует в URL
            if (url.contains("{id}")) {
                url = url.replace("{id}", String.valueOf(id));
            }
            return given()
                    .spec(requestSpecification)
                    .body(model)
                    .put(url)
                    .then()
                    .assertThat()
                    .spec(responseSpecification);
        });
    }

    @Override
    @Step("DELETE запрос на {endpoint} с id {id}")
    public Object delete(long id) {
        return null;
    }

    @Override
    @Step("GET запрос на {endpoint}")
    public ValidatableResponse getAll(Class<?> clazz) {
        return given()
                .spec(requestSpecification)
                .get(API_VERSION + endpoint.getUrl())
                .then().assertThat()
                .spec(responseSpecification);
    }
}

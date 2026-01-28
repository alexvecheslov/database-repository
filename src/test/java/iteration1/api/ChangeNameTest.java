package iteration1.api;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.generators.RandomModelGenerator;
import models.ChangeNameRequest;
import models.ChangeNameResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;

import java.util.stream.Stream;

public class ChangeNameTest extends BaseTest {

    @Test
    public void userCanChangeNameTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        ChangeNameRequest changeNameRequest = RandomModelGenerator.generate(ChangeNameRequest.class);

        // Обновляем имя через PUT
        new ValidatedCrudRequester<ChangeNameResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .update(0, changeNameRequest);

        // Получаем обновленный профиль через GET для проверки
        ChangeNameResponse updatedProfile = new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);

        ModelAssertions.assertThatModels(changeNameRequest, updatedProfile).match();
    }

    @ParameterizedTest
    @MethodSource("invalidName")
    public void userCanNotChangeToInvalidName(String invalidName, String errorType) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        ChangeNameResponse nameBeforeResponse = new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(invalidName)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorType))
                .update(0, changeNameRequest);

        ChangeNameResponse nameAfterResponse = new CrudRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);

        ModelAssertions.assertThatModels(nameBeforeResponse, nameAfterResponse).match();
    }

    public static Stream<Arguments> invalidName() {
        return Stream.of(
                Arguments.of("", "Name must contain two words with letters only"),
                Arguments.of("5414141alex", "Name must contain two words with letters only"),
                Arguments.of("5414141alex$$$", "Name must contain two words with letters only"),
                Arguments.of("alexpetrov", "Name must contain two words with letters only"));

    }
}

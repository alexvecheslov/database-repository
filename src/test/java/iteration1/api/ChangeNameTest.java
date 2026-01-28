package iteration1.api;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.dao.UserDao;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.generators.RandomModelGenerator;
import api.models.ChangeNameRequest;
import api.models.ChangeNameResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        // Проверяем, что имя изменилось в базе данных
        UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
        assertEquals(changeNameRequest.getName(), userDao.getName(), 
                "Name should be updated in database after successful update");
    }

    @ParameterizedTest
    @MethodSource("invalidName")
    public void userCanNotChangeToInvalidName(String invalidName, String errorType) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        // Получаем имя до попытки изменения из БД
        UserDao userDaoBefore = DataBaseSteps.getUserByUsername(userRequest.getUsername());
        String nameBefore = userDaoBefore.getName();

        ChangeNameRequest changeNameRequest = ChangeNameRequest.builder()
                .name(invalidName)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorType))
                .update(0, changeNameRequest);

        // Проверяем, что имя не изменилось в базе данных
       UserDao userDao = DataBaseSteps.getUserByUsername(userRequest.getUsername());
       assertEquals(nameBefore, userDao.getName(),
               "Name should not be changed in database after invalid update attempt");
    }

    public static Stream<Arguments> invalidName() {
        return Stream.of(
                Arguments.of("", "Name must contain two words with letters only"),
                Arguments.of("5414141alex", "Name must contain two words with letters only"),
                Arguments.of("5414141alex$$$", "Name must contain two words with letters only"),
                Arguments.of("alexpetrov", "Name must contain two words with letters only"));

    }
}

package iteration1.api;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.dao.AccountDao;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import api.models.MakeDepositRequest;
import api.models.MakeDepositResponse;
import api.models.comparison.ModelAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MakeDepositTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("validBalanceData")
    public void userCanMakeDepositTest(double balance) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int accountId = (int) accountResponse.getId();

        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        MakeDepositResponse depositResponse = new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        ModelAssertions.assertThatModels(depositRequest, depositResponse).match();

        // Проверяем, что баланс изменился в базе данных
       AccountDao senderAccountDao = DataBaseSteps.getAccountById((long) accountId);
       assertEquals(depositResponse.getBalance(), senderAccountDao.getBalance(),
               "Account balance should match in database after deposit");
    }

    @ParameterizedTest
    @MethodSource("invalidBalanceData")
    public void userCannotDepositInvalidBalanceTest(double balance, String errorValue) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int accountId = (int) accountResponse.getId();

        // Получаем баланс до попытки депозита из БД
       AccountDao accountDaoBefore = DataBaseSteps.getAccountById((long) accountId);
       Double senderBalanceBefore = accountDaoBefore.getBalance();

        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(accountId)
                .balance(balance)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorValue))
                .post(depositRequest);

        // Проверяем, что баланс не изменился в базе данных
       AccountDao accountDaoAfter = DataBaseSteps.getAccountById((long) accountId);
       assertEquals(senderBalanceBefore, accountDaoAfter.getBalance(),
        "Account balance should not be changed in database after invalid deposit attempt");
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAccount")
    public void userCannotDepositToInvalidAccountTest(int account, int balance) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse accountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int accountId = (int) accountResponse.getId();

        // Получаем баланс до попытки депозита из БД
        AccountDao accountDaoBefore = DataBaseSteps.getAccountById((long) accountId);
        Double balanceBefore = accountDaoBefore.getBalance();

        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(account)
                .balance(balance)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsForbidden())
                .post(depositRequest);

        // Проверяем, что баланс не изменился в базе данных
        AccountDao accountDaoAfter = DataBaseSteps.getAccountById((long) accountId);
        assertEquals(balanceBefore, accountDaoAfter.getBalance(), 
                "Account balance should not be changed in database after deposit to invalid account");
    }

    public static Stream<Arguments> invalidDepositAccount() {
        return Stream.of(
                Arguments.of(2, 1000),
                Arguments.of(4, 1000));
    }

    public static Stream<Arguments> validBalanceData() {
        return Stream.of(
                Arguments.of(4000),
                Arguments.of(4999.99),
                Arguments.of(0.01));

    }

    public static Stream<Arguments> invalidBalanceData() {
        return Stream.of(
                Arguments.of(5000.01, "Deposit amount exceeds the 5000 limit"),
                Arguments.of(-0.01, "Invalid account or amount"),
                Arguments.of(0, "Invalid account or amount"));

    }
}
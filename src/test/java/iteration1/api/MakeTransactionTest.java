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
import api.models.MakeTransactionRequest;
import api.models.MakeTransactionResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MakeTransactionTest extends BaseTest {

    @ParameterizedTest
    @MethodSource("validTransactionAmountSmall")
    public void userCanMakeTransactionWithSmallAmountTest(double amount) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse firstAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int firstAccountId = (int) firstAccountResponse.getId();

        CreateAccountResponse secondAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int secondAccountId = (int) secondAccountResponse.getId();

        double depositAmount = amount + 100.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(firstAccountId)
                .balance(depositAmount)
                .build();
        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        // Получаем балансы до транзакции из БД
        AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountById((long) firstAccountId);
        AccountDao receiverAccountDaoBefore = DataBaseSteps.getAccountById((long) secondAccountId);
        Double senderBalanceBefore = senderAccountDaoBefore.getBalance();
        Double receiverBalanceBefore = receiverAccountDaoBefore.getBalance();


        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(amount)
                .build();

        new ValidatedCrudRequester<MakeTransactionResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transactionRequest);

        // Проверяем, что балансы изменились в базе данных
        AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountById((long) firstAccountId);
        AccountDao receiverAccountDaoAfter = DataBaseSteps.getAccountById((long) secondAccountId);
        assertEquals(senderBalanceBefore - amount, senderAccountDaoAfter.getBalance(),
                "Sender account balance should be decreased by transaction amount in database");
        assertEquals(receiverBalanceBefore + amount, receiverAccountDaoAfter.getBalance(),
                "Receiver account balance should be increased by transaction amount in database");
    }

    @ParameterizedTest
    @MethodSource("validTransactionAmountLarge")
    public void userCanMakeTransactionWithLargeAmountTest(double amount) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse firstAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int firstAccountId = (int) firstAccountResponse.getId();

        CreateAccountResponse secondAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int secondAccountId = (int) secondAccountResponse.getId();

        double remainingNeeded = amount + 100.0;
        while (remainingNeeded > 0) {
            double currentDeposit = Math.min(remainingNeeded, 5000.0);
            MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                    .id(firstAccountId)
                    .balance(currentDeposit)
                    .build();
            new ValidatedCrudRequester<MakeDepositResponse>(
                    RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                    Endpoint.DEPOSIT,
                    ResponseSpecs.requestReturnsOK())
                    .post(depositRequest);
            remainingNeeded -= currentDeposit;
        }

        // Получаем балансы до транзакции из БД
        AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountById((long) firstAccountId);
        AccountDao receiverAccountDaoBefore = DataBaseSteps.getAccountById((long) secondAccountId);
        Double senderBalanceBefore = senderAccountDaoBefore.getBalance();
        Double receiverBalanceBefore = receiverAccountDaoBefore.getBalance();

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(amount)
                .build();

        new ValidatedCrudRequester<MakeTransactionResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transactionRequest);

        // Проверяем, что балансы изменились в базе данных
       AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountById((long) firstAccountId);
       AccountDao receiverAccountDaoAfter = DataBaseSteps.getAccountById((long) secondAccountId);
       assertEquals(senderBalanceBefore - amount, senderAccountDaoAfter.getBalance(),
               "Sender account balance should be decreased by transaction amount in database");
       assertEquals(receiverBalanceBefore + amount, receiverAccountDaoAfter.getBalance(),
               "Receiver account balance should be increased by transaction amount in database");
    }


    public static Stream<Arguments> validTransactionAmountSmall() {
        return Stream.of(
                Arguments.of(500),
                Arguments.of(0.01));
    }

    public static Stream<Arguments> validTransactionAmountLarge() {
        return Stream.of(
                Arguments.of(9999.99));
    }

    @ParameterizedTest
    @MethodSource("invalidAmountTransfer")
    public void userCannotTransferInvalidAmountTest(double amount, String errorType) {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse firstAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int firstAccountId = (int) firstAccountResponse.getId();

        CreateAccountResponse secondAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int secondAccountId = (int) secondAccountResponse.getId();

        double depositAmount = 1000.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(firstAccountId)
                .balance(depositAmount)
                .build();

        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        // Получаем балансы до попытки транзакции из БД
      AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountById((long) firstAccountId);
      AccountDao receiverAccountDaoBefore = DataBaseSteps.getAccountById((long) secondAccountId);
      Double senderBalanceBefore = senderAccountDaoBefore.getBalance();
      Double receiverBalanceBefore = receiverAccountDaoBefore.getBalance();

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(amount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestWithMessage(errorType))
                .post(transactionRequest);

        // Проверяем, что балансы не изменились в базе данных
       AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountById((long) firstAccountId);
       AccountDao receiverAccountDaoAfter = DataBaseSteps.getAccountById((long) secondAccountId);
       assertEquals(senderBalanceBefore, senderAccountDaoAfter.getBalance(),
               "Sender account balance should not be changed in database after invalid transaction attempt");
       assertEquals(receiverBalanceBefore, receiverAccountDaoAfter.getBalance(),
               "Receiver account balance should not be changed in database after invalid transaction attempt");
    }

    public static Stream<Arguments> invalidAmountTransfer() {
        return Stream.of(
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(0, "Transfer amount must be at least 0.01"),
                Arguments.of(-0.01, "Transfer amount must be at least 0.01"));
    }

    @Test
    public void userCanTransferToDifferentAccountTest() {
        CreateUserRequest senderUserRequest = AdminSteps.createUser();
        CreateUserRequest receiverUserRequest = AdminSteps.createUser();

        CreateAccountResponse senderAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int senderAccountId = (int) senderAccountResponse.getId();

        CreateAccountResponse receiverAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(receiverUserRequest.getUsername(), receiverUserRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int receiverAccountId = (int) receiverAccountResponse.getId();

        double transferAmount = 500.0;
        double depositAmount = transferAmount + 100.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(senderAccountId)
                .balance(depositAmount)
                .build();

        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        // Получаем балансы до транзакции из БД
        AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountById((long) senderAccountId);
        AccountDao receiverAccountDaoBefore = DataBaseSteps.getAccountById((long) receiverAccountId);
        Double senderBalanceBefore = senderAccountDaoBefore.getBalance();
        Double receiverBalanceBefore = receiverAccountDaoBefore.getBalance();

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(transferAmount)
                .build();

        new ValidatedCrudRequester<MakeTransactionResponse>(
                RequestSpecs.authAsUser(senderUserRequest.getUsername(), senderUserRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsOK())
                .post(transactionRequest);

        // Проверяем, что балансы изменились в базе данных
        AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountById((long) senderAccountId);
        AccountDao receiverAccountDaoAfter = DataBaseSteps.getAccountById((long) receiverAccountId);
        
        assertEquals(senderBalanceBefore - transferAmount, senderAccountDaoAfter.getBalance(), 
                "Sender account balance should be decreased by transaction amount in database");
        assertEquals(receiverBalanceBefore + transferAmount, receiverAccountDaoAfter.getBalance(), 
                "Receiver account balance should be increased by transaction amount in database");
    }

    @Test
    public void userCannotTransferMoreThanAccountBalanceTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        CreateAccountResponse firstAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int firstAccountId = (int) firstAccountResponse.getId();

        CreateAccountResponse secondAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        int secondAccountId = (int) secondAccountResponse.getId();

        double depositAmount = 500.0;
        double transferAmount = depositAmount + 100.0;
        MakeDepositRequest depositRequest = MakeDepositRequest.builder()
                .id(firstAccountId)
                .balance(depositAmount)
                .build();

        new ValidatedCrudRequester<MakeDepositResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        // Получаем балансы до попытки транзакции из БД
        AccountDao senderAccountDaoBefore = DataBaseSteps.getAccountById((long) firstAccountId);
        AccountDao receiverAccountDaoBefore = DataBaseSteps.getAccountById((long) secondAccountId);
        Double senderBalanceBefore = senderAccountDaoBefore.getBalance();
        Double receiverBalanceBefore = receiverAccountDaoBefore.getBalance();

        MakeTransactionRequest transactionRequest = MakeTransactionRequest.builder()
                .senderAccountId(firstAccountId)
                .receiverAccountId(secondAccountId)
                .amount(transferAmount)
                .build();

        new CrudRequester(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                Endpoint.TRANSFER,
                ResponseSpecs.requestReturnsBadRequestWithMessage("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transactionRequest);

        // Проверяем, что балансы не изменились в базе данных
        AccountDao senderAccountDaoAfter = DataBaseSteps.getAccountById((long) firstAccountId);
        AccountDao receiverAccountDaoAfter = DataBaseSteps.getAccountById((long) secondAccountId);
        
        assertEquals(senderBalanceBefore, senderAccountDaoAfter.getBalance(), 
                "Sender account balance should not be changed in database after insufficient funds transaction attempt");
        assertEquals(receiverBalanceBefore, receiverAccountDaoAfter.getBalance(), 
                "Receiver account balance should not be changed in database after insufficient funds transaction attempt");
    }
}
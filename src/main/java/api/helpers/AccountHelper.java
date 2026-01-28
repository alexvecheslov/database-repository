package api.helpers;

import api.models.CreateAccountResponse;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;
import models.ChangeNameResponse;
import models.MakeDepositResponse;
import api.requests.skelethon.Endpoint;

import java.util.List;

public class AccountHelper {
    private AccountHelper() {}

    private static List<MakeDepositResponse> getAccountsList(String username, String password) {
        ChangeNameResponse profile = new CrudRequester(
                RequestSpecs.authAsUser(username, password),
                Endpoint.PROFILE,
                ResponseSpecs.requestReturnsOK())
                .getAll(ChangeNameResponse.class)
                .extract()
                .as(ChangeNameResponse.class);

        return profile.getAccounts();
    }

    public static double getAccountBalance(int accountId, String username, String password) {
        MakeDepositResponse account = getAccountById(accountId, username, password);
        return account.getBalance();
    }

    public static MakeDepositResponse getAccountById(int accountId, String username, String password) {
        List<MakeDepositResponse> accounts = getAccountsList(username, password);

        return accounts.stream()
                .filter(acc -> acc.getId() == accountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));
    }

    public static List<Integer> getAccountIds(String username, String password) {
        return getAccountsList(username, password).stream()
                .map(MakeDepositResponse::getId)
                .toList();
    }

    public static CreateAccountResponse getCreateAccountResponseById(long accountId, String username, String password) {
        MakeDepositResponse account = getAccountsList(username, password).stream()
                .filter(acc -> acc.getId() == accountId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Account not found: " + accountId));

        return CreateAccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .transactions(account.getTransactions() != null ? 
                        account.getTransactions() : List.<Object>of())
                .build();
    }
}

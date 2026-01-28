package api.requests.skelethon;

import api.models.BaseModel;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import api.models.CreateAccountResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import models.ChangeNameResponse;
import models.MakeDepositResponse;
import models.MakeTransactionResponse;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),

    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),

    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            CreateAccountResponse.class
    ),

    PROFILE(
            "/customer/profile",
            BaseModel.class,
            ChangeNameResponse.class
    ),

    DEPOSIT(
            "/accounts/deposit",
            BaseModel.class,
            MakeDepositResponse.class

    ),

    TRANSFER(
            "/accounts/transfer",
            BaseModel.class,
            MakeTransactionResponse.class
    );


    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
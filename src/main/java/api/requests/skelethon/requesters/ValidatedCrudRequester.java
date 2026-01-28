package api.requests.skelethon.requesters;

import api.requests.skelethon.interfaces.GetAllEndpointInterface;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.BaseModel;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.HttpRequest;
import api.requests.skelethon.interfaces.CrudEndpointInterface;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import api.models.MakeDepositResponse;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class ValidatedCrudRequester<T extends BaseModel> extends HttpRequest implements CrudEndpointInterface, GetAllEndpointInterface {
    private CrudRequester crudRequester;
    private static final ObjectMapper customObjectMapper = createCustomObjectMapper();

    private static ObjectMapper createCustomObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxNestingDepth(2000)
                        .build()
        );
        // Настраиваем для обработки циклических ссылок
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }
    
    // Специальная обработка для MakeDepositResponse с циклическими ссылками
    // Используем регулярные выражения для извлечения полей без полного парсинга JSON
    private static MakeDepositResponse deserializeMakeDepositResponse(String json) {
        MakeDepositResponse response = new MakeDepositResponse();
        
        try {
            // Извлекаем основные поля через регулярные выражения из корневого объекта
            // Ищем первое вхождение каждого поля (корневой уровень)
            java.util.regex.Pattern rootIdPattern = java.util.regex.Pattern.compile("^\\s*\\{\\s*\"id\"\\s*:\\s*(\\d+)");
            java.util.regex.Pattern rootAccountNumberPattern = java.util.regex.Pattern.compile("\"accountNumber\"\\s*:\\s*\"([^\"]+)\"");
            java.util.regex.Pattern rootBalancePattern = java.util.regex.Pattern.compile("\"balance\"\\s*:\\s*([0-9.]+)");
            
            java.util.regex.Matcher rootIdMatcher = rootIdPattern.matcher(json);
            if (rootIdMatcher.find()) {
                response.setId(Integer.parseInt(rootIdMatcher.group(1)));
            }
            
            // Ищем accountNumber и balance на корневом уровне (до первого transactions)
            int transactionsIndex = json.indexOf("\"transactions\"");
            String rootPart = transactionsIndex > 0 ? json.substring(0, transactionsIndex) : json;
            
            java.util.regex.Matcher accountNumberMatcher = rootAccountNumberPattern.matcher(rootPart);
            if (accountNumberMatcher.find()) {
                response.setAccountNumber(accountNumberMatcher.group(1));
            }
            
            java.util.regex.Matcher balanceMatcher = rootBalancePattern.matcher(rootPart);
            if (balanceMatcher.find()) {
                response.setBalance(Double.parseDouble(balanceMatcher.group(1)));
            }
            
            // Для transactions извлекаем только первую транзакцию без циклических ссылок
            // Используем паттерн для поиска первой транзакции в массиве
            java.util.regex.Pattern firstTransactionPattern = java.util.regex.Pattern.compile(
                "\"transactions\"\\s*:\\s*\\[\\s*\\{\\s*\"id\"\\s*:\\s*(\\d+)\\s*,\\s*\"amount\"\\s*:\\s*([0-9.]+)\\s*,\\s*\"type\"\\s*:\\s*\"([^\"]+)\"\\s*,\\s*\"timestamp\"\\s*:\\s*\"([^\"]+)\""
            );
            
            java.util.regex.Matcher transactionMatcher = firstTransactionPattern.matcher(json);
            List<Object> transactions = new ArrayList<>();
            if (transactionMatcher.find()) {
                // Создаем упрощенную транзакцию без relatedAccount
                String simplifiedTransaction = String.format(
                    "{\"id\":%s,\"amount\":%s,\"type\":\"%s\",\"timestamp\":\"%s\"}",
                    transactionMatcher.group(1),
                    transactionMatcher.group(2),
                    transactionMatcher.group(3),
                    transactionMatcher.group(4)
                );
                transactions.add(simplifiedTransaction);
            }
            response.setTransactions(transactions);
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize MakeDepositResponse: " + e.getMessage(), e);
        }
    }

    public ValidatedCrudRequester(RequestSpecification requestSpecification, Endpoint endpoint, ResponseSpecification responseSpecification) {
        super(requestSpecification, endpoint, responseSpecification);
        this.crudRequester = new CrudRequester(requestSpecification, endpoint, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {
        // Используем кастомный ObjectMapper для десериализации
        io.restassured.response.ValidatableResponse validatableResponse = crudRequester.post(model);
        String json;
        try {
            json = validatableResponse.extract().asString();
        } catch (Exception e) {
            // Если не удалось прочитать тело ответа (например, из-за MalformedChunkCodingException),
            // пытаемся прочитать его через getBody().asString()
            try {
                json = validatableResponse.extract().response().getBody().asString();
            } catch (Exception ex) {
                // Если и это не помогло, пробуем прочитать через байты
                try {
                    byte[] bytes = validatableResponse.extract().response().asByteArray();
                    json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e2) {
                    throw new RuntimeException("Failed to read response body after multiple attempts", e2);
                }
            }
        }
        try {
            // Специальная обработка для MakeDepositResponse с циклическими ссылками
            if (endpoint.getResponseModel() == MakeDepositResponse.class) {
                return (T) deserializeMakeDepositResponse(json);
            }
            return (T) customObjectMapper.readValue(json, endpoint.getResponseModel());
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response: " + (json.length() > 500 ? json.substring(0, 500) + "..." : json), e);
        }
    }

    @Override
    public T get(long id) {
        io.restassured.response.ValidatableResponse validatableResponse = crudRequester.get(id);
        String json;
        try {
            json = validatableResponse.extract().asString();
        } catch (Exception e) {
            // Если не удалось прочитать тело ответа (например, из-за MalformedChunkCodingException),
            // пытаемся прочитать его через getBody().asString()
            try {
                json = validatableResponse.extract().response().getBody().asString();
            } catch (Exception ex) {
                // Если и это не помогло, пробуем прочитать через байты
                try {
                    byte[] bytes = validatableResponse.extract().response().asByteArray();
                    json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e2) {
                    throw new RuntimeException("Failed to read response body after multiple attempts", e2);
                }
            }
        }
        try {
            // Специальная обработка для MakeDepositResponse с циклическими ссылками
            if (endpoint.getResponseModel() == MakeDepositResponse.class) {
                return (T) deserializeMakeDepositResponse(json);
            }
            return (T) customObjectMapper.readValue(json, endpoint.getResponseModel());
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response: " + (json.length() > 500 ? json.substring(0, 500) + "..." : json), e);
        }
    }

    @Override
    public T update(long id, BaseModel model) {
        io.restassured.response.ValidatableResponse validatableResponse = (io.restassured.response.ValidatableResponse) crudRequester.update(id, model);
        String json;
        try {
            json = validatableResponse.extract().asString();
        } catch (Exception e) {
            // Если не удалось прочитать тело ответа (например, из-за MalformedChunkCodingException),
            // пытаемся прочитать его через getBody().asString()
            try {
                json = validatableResponse.extract().response().getBody().asString();
            } catch (Exception ex) {
                // Если и это не помогло, пробуем прочитать через байты
                try {
                    byte[] bytes = validatableResponse.extract().response().asByteArray();
                    json = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
                } catch (Exception e2) {
                    throw new RuntimeException("Failed to read response body after multiple attempts", e2);
                }
            }
        }
        try {
            // Специальная обработка для MakeDepositResponse с циклическими ссылками
            if (endpoint.getResponseModel() == MakeDepositResponse.class) {
                return (T) deserializeMakeDepositResponse(json);
            }
            return (T) customObjectMapper.readValue(json, endpoint.getResponseModel());
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize response: " + (json.length() > 500 ? json.substring(0, 500) + "..." : json), e);
        }
    }

    @Override
    public Object delete(long id) {
        return null;
    }

    @Override
    public List<T> getAll(Class<?> clazz) {
        T[] array = (T[]) crudRequester.getAll(clazz).extract().as(clazz);
        return Arrays.asList(array);
    }
}

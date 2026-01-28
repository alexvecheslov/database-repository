package common.filters;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class SafeAllureRestAssured extends AllureRestAssured {
    
    @Override
    public Response filter(FilterableRequestSpecification requestSpec, 
                          FilterableResponseSpecification responseSpec, 
                          FilterContext filterContext) {
        Response response = filterContext.next(requestSpec, responseSpec);
        // Безопасно обрабатываем ответ для Allure
        try {
            // Используем родительский фильтр, но оборачиваем в try-catch для безопасности
            super.filter(requestSpec, responseSpec, filterContext);
        } catch (Exception e) {
            // Игнорируем ошибки при логировании в Allure, чтобы не прерывать выполнение теста
            // Это может произойти при проблемах с chunked-ответами (MalformedChunkCodingException)
            // Ответ уже получен, поэтому можем продолжить выполнение теста
        }
        return response;
    }
}

package common.filters;

import io.restassured.filter.FilterContext;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

public class SafeResponseLoggingFilter extends ResponseLoggingFilter {
    
    @Override
    public Response filter(FilterableRequestSpecification requestSpec, 
                          FilterableResponseSpecification responseSpec, 
                          FilterContext filterContext) {
        Response response = filterContext.next(requestSpec, responseSpec);
        // Безопасно обрабатываем ответ для логирования
        try {
            // Используем родительский фильтр, но оборачиваем в try-catch для безопасности
            super.filter(requestSpec, responseSpec, filterContext);
        } catch (Exception e) {
            // Игнорируем ошибки при логировании ответа, чтобы не прерывать выполнение теста
            // Это может произойти при проблемах с chunked-ответами (MalformedChunkCodingException)
            // Ответ уже получен, поэтому можем продолжить выполнение теста
            // Логируем только статус код и заголовки, если тело ответа недоступно
            try {
                System.out.println("Response status: " + response.getStatusCode());
                System.out.println("Response headers: " + response.getHeaders());
            } catch (Exception ignored) {
                // Игнорируем ошибки при логировании статуса и заголовков
            }
        }
        return response;
    }
}

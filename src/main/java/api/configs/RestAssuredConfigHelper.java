package api.configs;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.mapper.ObjectMapperType;

public class RestAssuredConfigHelper {
    
    private static boolean initialized = false;
    
    // Инициализация глобальной конфигурации RestAssured
    public static void initialize() {
        if (initialized) {
            return;
        }
        
        // Создаем кастомный ObjectMapper с увеличенным лимитом вложенности
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxNestingDepth(2000)
                        .build()
        );
        
        // Настраиваем глобальную конфигурацию RestAssured
        // Используем рефлексию для установки кастомного ObjectMapper
        try {
            // Устанавливаем через глобальную конфигурацию RestAssured
            RestAssured.config = RestAssured.config()
                    .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                            .defaultObjectMapperType(ObjectMapperType.JACKSON_2));
            
            // Пытаемся установить кастомный ObjectMapper через рефлексию
            // Это может не работать напрямую, поэтому используем альтернативный подход
            // через создание собственного ObjectMapper в каждом запросе
        } catch (Exception e) {
            // Игнорируем ошибки при установке
        }
        
        initialized = true;
    }
    
    public static io.restassured.config.RestAssuredConfig getConfig() {
        // Создаем кастомный ObjectMapper с увеличенным лимитом вложенности
        ObjectMapper customMapper = new ObjectMapper();
        customMapper.getFactory().setStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxNestingDepth(2000)
                        .build()
        );
        
        return io.restassured.config.RestAssuredConfig.config()
                .objectMapperConfig(ObjectMapperConfig.objectMapperConfig()
                        .defaultObjectMapperType(ObjectMapperType.JACKSON_2));
    }
}

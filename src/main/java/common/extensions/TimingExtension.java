package common.extensions;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.HashMap;
import java.util.Map;

public class TimingExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterAllCallback {
    private Map<String, Long> startTimes = new HashMap<>();
    private Map<String, Long> durationTimes = new HashMap<>();

    private String getTestKey(ExtensionContext extensionContext) {
        return extensionContext.getRequiredTestClass().getName() + "." + extensionContext.getDisplayName();
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        String key = getTestKey(extensionContext);
        startTimes.put(key, System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        String key = getTestKey(extensionContext);
        Long startTime = startTimes.get(key);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            durationTimes.put(key, duration);
            startTimes.remove(key); // Очищаем использованное значение
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        durationTimes.forEach( (testName, duration) ->
                System.out.println("Test '" + testName + "' took " + duration + " ms")
        );
    }
}

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


    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        startTimes.put(extensionContext.getRequiredTestClass().getPackageName() + extensionContext.getDisplayName(), System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
        long startTime = startTimes.get(extensionContext.getRequiredTestClass().getPackageName() + extensionContext.getDisplayName());
        long duration = System.currentTimeMillis() - startTime;
        durationTimes.put(extensionContext.getRequiredTestClass().getPackageName() + extensionContext.getDisplayName(), duration);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        durationTimes.forEach( (testName, duration) ->
                System.out.println("Test '" + testName + "' took " + duration + " ms")
        );
    }
}

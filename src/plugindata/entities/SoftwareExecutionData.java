package plugindata.entities;

import java.util.ArrayList;
import java.util.List;

public class SoftwareExecutionData {

    private List<ExecutionDataEvent> methodCalls;

    public SoftwareExecutionData() {

        methodCalls = new ArrayList<>();
    }

    public List<ExecutionDataEvent> getData() {
        return methodCalls;
    }

    public void addMethodCall(ExecutionDataEvent event) {
        methodCalls.add(event);
    }
}

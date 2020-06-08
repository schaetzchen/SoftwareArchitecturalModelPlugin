package plugindata.utils.component_identification;

import plugindata.entities.ExecutionDataEvent;

import java.util.ArrayList;

public class ComponentExecutionData {

    private String componentName;
    private ArrayList<ExecutionDataEvent> executionData;

    ComponentExecutionData(String componentName, ArrayList<ExecutionDataEvent> executionData) {
        this.componentName = componentName;
        this.executionData = executionData;
    }

    public ArrayList<ExecutionDataEvent> getExecutionData() {
        return executionData;
    }

    public String getComponentName() {
        return componentName;
    }
}

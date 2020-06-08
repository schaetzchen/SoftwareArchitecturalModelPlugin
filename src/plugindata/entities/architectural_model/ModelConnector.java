package plugindata.entities.architectural_model;

import java.util.Set;

public class ModelConnector implements IModelNode {

    private String name;
    private ConnectorDotData dotData;
    private ArchitecturalModelImpl model;

    public ModelConnector(ArchitecturalModelImpl model, ConnectorDotData dotData) {
        this.model = model;
        this.dotData = dotData;
    }

    void setName(String name) {
        this.name = name;
        dotData.setHeading(name);
    }

    public ConnectorDotData getDotData() {
        return dotData;
    }

    @Override
    public String getName() {
        return name;
    }
}

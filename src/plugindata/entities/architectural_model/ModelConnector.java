package plugindata.entities.architectural_model;

import java.util.Set;

public class ModelConnector implements IModelNode {

    private String name;
    private DotFileClusterData dotData;
    private ArchitecturalModelImpl model;

    public ModelConnector(ArchitecturalModelImpl model, DotFileClusterData dotData) {
        this.model = model;
        this.dotData = dotData;
        dotData.addAttribute("color=blue");
    }

    void setName(String name) {
        this.name = name;
        dotData.setHeading(name);
    }

    public DotFileClusterData getDotData() {
        return dotData;
    }

    @Override
    public String getName() {
        return name;
    }
}

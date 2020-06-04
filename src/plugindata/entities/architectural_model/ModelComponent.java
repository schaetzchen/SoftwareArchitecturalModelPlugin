package plugindata.entities.architectural_model;

import plugindata.entities.SoftwareComponent;
import plugindata.entities.SoftwareInterface;

public class ModelComponent implements IModelNode {

    private SoftwareComponent component;
    private DotFileClusterData dotData;
    private ArchitecturalModelImpl model;

    public ModelComponent(ArchitecturalModelImpl model, SoftwareComponent component) {
        this.model = model;
        this.component = component;
        dotData = constructDotData();
    }

    private DotFileClusterData constructDotData() {

        DotFileClusterData res = new DotFileClusterData(component.getName());

        for (SoftwareInterface softwareInterface : component.getInterfaces())
            if (softwareInterface.getMethodNames().size() > 0)
                res.addNode(softwareInterface.getName(), "box",
                        softwareInterface.getName().substring(softwareInterface.getName().lastIndexOf("_") + 1));

        return res;
    }

    public DotFileClusterData getDotData() {
        return dotData;
    }

    @Override
    public String getName() {
        return component.getName();
    }
}

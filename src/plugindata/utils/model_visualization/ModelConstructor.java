package plugindata.utils.model_visualization;

import plugindata.entities.SoftwareComponent;
import plugindata.entities.SoftwareInterface;
import plugindata.entities.SoftwareMethod;
import plugindata.entities.architectural_model.ArchitecturalModelImpl;
import plugindata.entities.architectural_model.ModelComponent;
import plugindata.entities.architectural_model.ModelConnector;

import java.io.File;
import java.util.Set;

public class ModelConstructor {

    public static ArchitecturalModelImpl constructModel(Set<SoftwareComponent> components,
                                                        Set<SoftwareMethod> interactionMethods, String dirPath) {

        ArchitecturalModelImpl model = new ArchitecturalModelImpl();
        constructComponentNodes(model, components);
        addConnectorModels(model, dirPath, interactionMethods);

        return model;
    }

    private static void constructComponentNodes(ArchitecturalModelImpl model, Set<SoftwareComponent> components) {
        for (SoftwareComponent component : components)
            model.addComponent(new ModelComponent(model, component));
    }

    private static void addConnectorModels(ArchitecturalModelImpl model, String dirPath,
                                           Set<SoftwareMethod> interactionMethods) {

        File dir = new File(dirPath);
        for (File file : dir.listFiles())
            if (file.getName().toLowerCase().endsWith(".dot")) {
                ModelConnector connector = new ModelConnector(model, DotFileParser.parseFile(file.getPath()));
                model.addConnector(connector);

                String methodName = file.getName().substring(0, file.getName().lastIndexOf('.')).replace('-', '.');
                String[] arr = methodName.split("\\.");
                methodName = arr[arr.length - 2] + "." + arr[arr.length - 1];

                for (SoftwareMethod existingMethod : interactionMethods)
                    if (existingMethod.getMethodName().contains(methodName)) {
                        SoftwareInterface source = existingMethod.getBelongingInterface();
                        String target = connector.getDotData().getNodes().get(0);
                        target = target.substring(0, target.indexOf(' ') - 1);
                        model.addArc("\"" + source.getName() + "\"", target, "lhead=\"cluster_" + connector.getName() + "\"");
                    }

                for (String node : connector.getDotData().getNodes())
                    if (node.contains("Interface")) {

                        String source = node.substring(0, node.indexOf(' '));
                        String temp = file.getName().substring(0, file.getName().lastIndexOf('.'));
                        String target = node.substring(0, node.indexOf(temp));
                        target = target.substring(0, target.length() - 1);
                        model.addArc(source, target);
                    }
            }
    }
}
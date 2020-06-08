package plugindata.entities.architectural_model;

import java.util.HashSet;
import java.util.Set;

public class ArchitecturalModelImpl {

    Set<ModelComponent> components;
    Set<ModelConnector> connectors;
    Set<ModelArc> arcs;

    public ArchitecturalModelImpl() {

        components = new HashSet<>();
        connectors = new HashSet<>();
        arcs = new HashSet<>();
    }

    public void addComponent(ModelComponent component) {
        components.add(component);
    }

    public void addConnector(ModelConnector connector) {
        connectors.add(connector);
        connector.setName("Connector" + connectors.size());
    }

    public String constructDotMessage() {

        String res = "digraph G {\ncompound=true;\n\n";
        for (ModelComponent component : components)
            res += component.getDotData().constructDotGraph();
        res += "\n";
        for (ModelConnector connector : connectors)
            res += connector.getDotData().constructDotGraph();
        res += "\n";
        for (ModelArc arc : arcs)
            res += arc.getDotCode();
        res += "}";
        return res;
    }

    public void addArc(String s, String t) {
        arcs.add(new ModelArc(s, t));
    }

    public void addArc(String s, String t, String attr) {
        arcs.add(new ModelArc(s, t, attr));
    }
}
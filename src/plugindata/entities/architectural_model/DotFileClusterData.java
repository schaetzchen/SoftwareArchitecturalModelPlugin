package plugindata.entities.architectural_model;

import java.util.ArrayList;
import java.util.List;

public class DotFileClusterData {

    private String heading;
    private String label;
    private String attribute;
    private List<String> nodes;
    private List<String> edges;

    public DotFileClusterData(String heading) {
        setHeading(heading);
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    public void setHeading(String heading) {
        this.heading = "subgraph \"cluster_" + heading + "\" {";
        label = "label = \"" + heading + "\"";
    }

    public void addNode(String nodeName, String shape, String label) {
        nodes.add("\"" + nodeName + "\" [shape=" + shape + ",label=\"" + label + "\",fontsize=10]");
    }

    public void addNode(String nodeName, String shape, String label, int peripheries) {
        nodes.add("\"" + nodeName + "\" [shape=" + shape + ",label=\"" + label +
                "\",fontsize=10,peripheries=" + peripheries + "]");
    }

    public void addEdge(String source, String target) {
        edges.add("\"" + source + "\" -> \"" + target + "\"");
    }

    public String constructDotGraph() {
        String res = heading + "\n";
        for (String node : nodes)
            res += node + "\n";
        res += "\n";
        for (String edge : edges)
            res += edge + "\n";
        res += "\n" + label + "\n";
        res += "\n" + attribute + "\n";
        res += "}\n";

        return res;
    }

    public void addAttribute(String attr) {
        attribute = attr;
    }

    public List<String> getNodes() {
        return nodes;
    }
}

package plugindata.utils.model_visualization;

import org.apache.commons.lang.StringUtils;
import plugindata.entities.architectural_model.DotFileClusterData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class DotFileParser {

    public static DotFileClusterData parseFile(String path) {

        DotFileClusterData clusterData = null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line = br.readLine();

            // Ignoring all lines until subgraph starts
            boolean subgraphStarted = false;
            while (line != null && !subgraphStarted) {

                if (line.contains("graph [")) {
                    String clusterName = StringUtils.substringBetween(line, "\"");
                    clusterData = new DotFileClusterData(clusterName.substring(clusterName.indexOf("_") + 1));
                    subgraphStarted = true;
                }
                line = br.readLine();
            }

            // Ignoring all lines until node info starts
            boolean nodesStarted = false;
            while (line != null && !nodesStarted) {

                if (line.contains("shape"))
                    nodesStarted = true;
                else
                    line = br.readLine();
            }

            // Reading node info
            boolean edgesStarted = false;
            while (line != null && !edgesStarted) {

                if (line.contains("->"))
                    edgesStarted = true;
                else if (line.contains("shape")) {
                    String nodeName = StringUtils.substringBetween(line, "\"");
                    String shape = "box";
                    if (line.contains("shape=circle"))
                        shape = "circle";
                    String label = "";

                    if (shape.equals("box")) {
                        label = nodeName.substring(0, nodeName.lastIndexOf(new File(path).getName().split("\\.")[0]) - 1);
                        clusterData.addNode(nodeName, shape, label, 2);
                    } else
                        clusterData.addNode(nodeName, shape, label);

                    line = br.readLine();
                } else
                    line = br.readLine();
            }

            // Reading edges info
            boolean subgraphEnded = false;
            while (line != null && !subgraphEnded) {

                if (line.contains("}"))
                    subgraphEnded = true;
                else if (line.contains("->")) {
                    String[] sourceAndTarget = line.split("->");
                    clusterData.addEdge(StringUtils.substringBetween(sourceAndTarget[0], "\""),
                            StringUtils.substringBetween(sourceAndTarget[1], "\""));
                    line = br.readLine();
                } else line = br.readLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return clusterData;
    }
}

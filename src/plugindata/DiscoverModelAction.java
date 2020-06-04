package plugindata;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import plugindata.entities.*;
import plugindata.entities.architectural_model.ArchitecturalModelImpl;
import plugindata.utils.PnmlFileHandler;
import plugindata.utils.component_identification.ComponentDataHandler;
import plugindata.utils.component_identification.ComponentExecutionData;
import plugindata.utils.interaction_methods_identification.InteractionLogHandler;
import plugindata.utils.interaction_methods_identification.InteractionMethodHandler;
import plugindata.utils.interface_identification.InterfaceDiscoveryHandler;
import plugindata.utils.model_visualization.ModelConstructor;
import plugindata.utils.model_visualization.ModelDialog;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiscoverModelAction extends AnAction {

    private static SoftwareExecutionData executionData;
    Project project;

    private void readFromFile(Project project) {

        try {
            BufferedReader br = new BufferedReader(new FileReader(project.getBasePath() + "/execution_data.csv"));
            br.readLine();

            String row = br.readLine();
            while (row != null) {

                try {

                    String[] data = row.split(",");

                    Integer calleeID = data[1].equals("-") ? Integer.MIN_VALUE : Integer.parseInt(data[1]);
                    Integer callerID = data[3].equals("-") ? Integer.MIN_VALUE : Integer.parseInt(data[3]);
                    Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(data[4]);
                    Integer caseID = Integer.parseInt(data[5]);

//                    events.add(new ExecutionDataEvent(data[0].split("\\.")[1], calleeID,
//                            data[2].split("\\.")[1], callerID, date, caseID));
                    executionData.addMethodCall(new ExecutionDataEvent(data[0], calleeID, data[2], callerID, date, caseID));
                }
                catch (NumberFormatException | ParseException | ArrayIndexOutOfBoundsException e) {
//                    e.printStackTrace();
                }

                try {
                    row = br.readLine();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }

            br.close();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        project = e.getProject();

        executionData = new SoftwareExecutionData();
        readFromFile(project);

        File directory_logs = new File(project.getBasePath() + "\\_behavioral_model_data\\logs");
        if (directory_logs.exists()) {
            for (File f : directory_logs.listFiles())
                f.delete();
            directory_logs.delete();
        }
        File directory_main = new File(project.getBasePath() + "\\_behavioral_model_data");
        if (directory_main.exists())
            directory_main.delete();

        Set<SoftwareInterface> interfaces = new HashSet<>();

        Graph methodCallingGraph = InteractionMethodHandler.getMethodCallingGraph(executionData);

        for (SoftwareComponent component : ComponentDataHandler.getComponents()) {

            ComponentExecutionData componentExecutionData = ComponentDataHandler.handleDataProjection(component.getName(), executionData.getData());
            Set<Pair<String, String>> topLevelMethodSet = InterfaceDiscoveryHandler.getTopLevelMethodSetOfAComponent(componentExecutionData, executionData, methodCallingGraph);
            Set<Set<String>> candidateInterfaceSet = InterfaceDiscoveryHandler.getCandidateInterfaceSet(topLevelMethodSet);
            List<Set<String>> mergedInterfaces = InterfaceDiscoveryHandler.mergeInterfaces(candidateInterfaceSet, 0.5);

            for (Set<String> interfaceMethods : mergedInterfaces) {
                SoftwareInterface newInterface = new SoftwareInterface(component, interfaceMethods);
                interfaces.add(newInterface);
            }
        }

        Set<SoftwareMethod> interactionMethods =
                InteractionMethodHandler.getInteractionMethods(interfaces, executionData, methodCallingGraph);

        Set<InteractionLogHandler> logs = new HashSet<>();

        for (SoftwareMethod method : interactionMethods)
            logs.add(new InteractionLogHandler(method, executionData, interfaces));

        if (!directory_main.exists())
            directory_main.mkdir();
        if (!directory_logs.exists())
            directory_logs.mkdir();

        exportInteractionLogs(logs);
        // Discover PNML models
        List<String> args = new ArrayList<>();
        args.add("python");
        String pathToFile = "";
        try {
            pathToFile = Paths.get(getClass().getClassLoader().getResource("model_discovery_handler.py").toURI()).toFile().getAbsolutePath();
        }
        catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        args.add(pathToFile);
        args.add(project.getBasePath() + "/_behavioral_model_data/logs");
        executeFile(args);
        // fix PNML files for parsing later
        for (File file : directory_logs.listFiles())
            if (file.getName().toLowerCase().endsWith(".pnml"))
                PnmlFileHandler.fixPnmlFile(file.getAbsolutePath());

        args = new ArrayList<>();
        args.add("java");
        args.add("-jar");
        pathToFile = "";
        try {
            pathToFile = Paths.get(getClass().getClassLoader().getResource("PNMLParseModule.jar").toURI()).toFile().getAbsolutePath();
        }
        catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        args.add(pathToFile);
        args.add("-a");

        for (File file : directory_logs.listFiles())
            if (file.getName().toLowerCase().endsWith(".pnml"))
                args.add(file.getAbsolutePath());
        args.add(directory_logs.getAbsolutePath());
        executeFile(args);

        ArchitecturalModelImpl model = ModelConstructor.constructModel(ComponentDataHandler.getComponents(),
                interactionMethods, directory_logs.getPath());

        writeToFile(model.constructDotMessage(), directory_logs.getPath(), "model.dot", true);

//        MutableGraph g = new Parser().read(model.constructDotMessage());
        try {
            Graphviz.fromFile(new File(directory_logs.getPath() + "\\model.dot")).render(Format.PNG).toFile(
                    new File(directory_logs.getPath() + "\\model.png"));
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }

        for (File f : directory_logs.listFiles())
            if (!(f.getName().equals("model.dot") || f.getName().equals("model.png")))
                f.delete();

        String interfaceData = "";

        List<SoftwareInterface> interfacesSorted = new ArrayList(interfaces);
        interfacesSorted.sort(Comparator.comparing(SoftwareInterface::getName));

        for (SoftwareInterface i : interfacesSorted) {
            interfaceData += i.getName() + " contains methods:\n";

            for (String method : i.getMethodNames())
                interfaceData += "\t" + method + "();\n";
        }
        writeToFile(interfaceData, directory_logs.getPath(), "interface_data.txt", true);

        new ModelDialog(directory_logs.getPath() + "\\model.png", interfaceData).show();
    }

    private void executeFile(List<String> args) {

        String[] argsArr = new String[args.size()];
        argsArr = args.toArray(argsArr);

        try {
            ProcessBuilder pb = new ProcessBuilder(argsArr);
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = in.readLine();
            while (line != null) {
                System.out.println(line);
                line = in.readLine();
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void exportInteractionLogs(Set<InteractionLogHandler> logs) {

        String dirPath = project.getBasePath() + "\\_behavioral_model_data\\logs";

        for (InteractionLogHandler logHandler : logs)
            if (!logHandler.getEventLog().getEvents().isEmpty()) {
                String[] temp = logHandler.getInteractionMethodName().split("\\.");
                String fileName = temp[temp.length - 2] + "-" +  temp[temp.length - 1] + ".csv";
//                String fileName = logHandler.getInteractionMethodName().
//                        substring(logHandler.getInteractionMethodName().lastIndexOf('.') + 1) + ".csv";
                writeToFile(constructMessage(logHandler.getEventLog()), dirPath, fileName, true);
            }
    }

    private String constructMessage(Log log) {

        String res = "case ID,activity name,transaction type,timestamp\n";

        for (LogEvent event : log.getEvents())
            res += event.getEventData() + "\n";

        return res;
    }

    private void writeToFile(String message, String dirPath, String fileName, boolean rewrite) {

        try {
            FileWriter csvWriter = new FileWriter(dirPath + "\\" + fileName, !rewrite);
            BufferedWriter bw = new BufferedWriter(csvWriter);

            bw.write(message);
            bw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}

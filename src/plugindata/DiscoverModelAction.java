package plugindata;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import plugindata.entities.*;
import plugindata.entities.architectural_model.ArchitecturalModelImpl;
import plugindata.utils.PnmlFileHandler;
import plugindata.utils.StringWrapper;
import plugindata.utils.component_identification.ComponentDataHandler;
import plugindata.utils.component_identification.ComponentExecutionData;
import plugindata.utils.interaction_methods_identification.InteractionLogHandler;
import plugindata.utils.interaction_methods_identification.InteractionMethodHandler;
import plugindata.utils.interface_identification.InterfaceDiscoveryHandler;
import plugindata.utils.model_visualization.ModelConstructor;
import plugindata.utils.model_visualization.ModelDialog;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiscoverModelAction extends AnAction {

    private static SoftwareExecutionData executionData;
    private Project project;
    private File directory_logs, directory_main;

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
                } catch (NumberFormatException | ParseException | ArrayIndexOutOfBoundsException e) {
//                    e.printStackTrace();
                }

                try {
                    row = br.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            br.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        project = e.getProject();
        ComponentDataHandler.retrievePackageDataInfo(project);
        if (executionData == null || executionData.getData().size() == 0) {
            executionData = new SoftwareExecutionData();
            readFromFile(project);
        }
        directory_logs = new File(project.getBasePath() + "\\_behavioral_model_data\\logs");
        directory_main = new File(project.getBasePath() + "\\_behavioral_model_data");
        clearTempDirectories();

        final StringWrapper interfaceData = new StringWrapper();

        ProgressManager.getInstance().run(new Task.Modal(project, "Discovering model", true) {
            public void run(ProgressIndicator indicator) {

                indicator.setText("Identifying interfaces...");

                Set<SoftwareInterface> interfaces = new HashSet<>();

                Graph methodCallingGraph = InteractionMethodHandler.getMethodCallingGraph(executionData.getData());

                for (SoftwareComponent component : ComponentDataHandler.getComponents()) {

                    component.removeAllInterfaces();

                    ComponentExecutionData componentExecutionData = ComponentDataHandler.handleDataProjection(component.getName(), executionData.getData());
                    Set<Pair<String, String>> topLevelMethodSet = InterfaceDiscoveryHandler.getTopLevelMethodSetOfAComponent(componentExecutionData, executionData, methodCallingGraph);
                    Set<Set<String>> candidateInterfaceSet = InterfaceDiscoveryHandler.getCandidateInterfaceSet(topLevelMethodSet);
                    List<Set<String>> mergedInterfaces = InterfaceDiscoveryHandler.mergeInterfaces(candidateInterfaceSet, 0.5);

                    for (Set<String> interfaceMethods : mergedInterfaces) {
                        SoftwareInterface newInterface = new SoftwareInterface(component, interfaceMethods);
                        interfaces.add(newInterface);
                    }
                }

                indicator.setFraction(0.2);
                indicator.setText("Identifying interaction methods...");

                Set<SoftwareMethod> interactionMethods =
                        InteractionMethodHandler.getInteractionMethods(interfaces, executionData, methodCallingGraph);
                indicator.setFraction(0.3);
                indicator.setText("Creating interaction logs...");

                Set<InteractionLogHandler> logs = new HashSet<>();

                for (SoftwareMethod method : interactionMethods)
                    logs.add(new InteractionLogHandler(method, executionData, interfaces));

                if (!directory_main.exists())
                    directory_main.mkdir();
                if (!directory_logs.exists())
                    directory_logs.mkdir();

                exportInteractionLogs(logs);
                indicator.setFraction(0.4);
                indicator.setText("Mining connector models...");

                // Discover PNML models
                List<String> args = new ArrayList<>();
                args.add("python");
                String pathToFile = "";
                try {
                    pathToFile = Paths.get(getClass().getClassLoader().getResource("model_discovery_handler.py").toURI()).toFile().getAbsolutePath();
                } catch (URISyntaxException ex) {
                    ex.printStackTrace();
                }
                args.add(pathToFile);
                args.add(project.getBasePath() + "/_behavioral_model_data/logs");
                executeFile(args);
                // fix PNML files for parsing later
                for (File file : directory_logs.listFiles())
                    if (file.getName().toLowerCase().endsWith(".pnml"))
                        PnmlFileHandler.fixPnmlFile(file.getAbsolutePath());
                indicator.setFraction(0.6);
                indicator.setText("Creating .dot descriptions...");

//                args = new ArrayList<>();
//                args.add("java");
//                args.add("-jar");
//                pathToFile = "";
//                try {
//                    pathToFile = Paths.get(getClass().getClassLoader().getResource("PNMLParseModule.jar").toURI()).toFile().getAbsolutePath();
//                } catch (URISyntaxException ex) {
//                    ex.printStackTrace();
//                }
//                args.add(pathToFile);
//                args.add("-a");
//
//                for (File file : directory_logs.listFiles())
//                    if (file.getName().toLowerCase().endsWith(".pnml"))
//                        args.add(file.getAbsolutePath());
//                args.add(directory_logs.getAbsolutePath());
//                executeFile(args);

                indicator.setFraction(0.8);
                indicator.setText("Building architectural model...");

                ArchitecturalModelImpl model = ModelConstructor.constructModel(ComponentDataHandler.getComponents(),
                        interactionMethods, directory_logs.getPath());

                writeToFile(model.constructDotMessage(), project.getBasePath(), "model.dot", true);

//        MutableGraph g = new Parser().read(model.constructDotMessage());
                try {
                    Graphviz.fromFile(new File(project.getBasePath() + "\\model.dot")).render(Format.PNG).toFile(
                            new File(project.getBasePath() + "\\model.png"));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                indicator.setFraction(0.9);
                indicator.setText("Exporting data...");

                List<SoftwareInterface> interfacesSorted = new ArrayList(interfaces);
                interfacesSorted.sort(Comparator.comparing(SoftwareInterface::getName));

                for (SoftwareInterface i : interfacesSorted) {
                    interfaceData.appendLine(i.getName() + " contains methods:\n");

                    for (String method : i.getMethodNames())
                        interfaceData.appendLine("\t" + method + "();\n");
                }
                writeToFile(interfaceData.getString(), project.getBasePath(), "interface_data.txt", true);

                indicator.setFraction(1.0);
            }
        });

        try {
            BufferedImage image = ImageIO.read(new File(project.getBasePath() + "\\model.png"));
            if (image.getHeight() <= 500 && image.getWidth() <= 750)
                new ModelDialog(project.getBasePath() + "\\model.png", interfaceData.getString()).show();
            else
                Messages.showMessageDialog("Architectural model was saved as a .png file in project base folder.", "Info", Messages.getInformationIcon());
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void clearTempDirectories() {
        if (directory_logs.exists()) {
            for (File f : directory_logs.listFiles())
                RunnableHelper.runWriteCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        RunnableHelper.deleteFile(this, f.getPath());
                    }
                });
            RunnableHelper.runWriteCommand(project, new Runnable() {
                @Override
                public void run() {
                    RunnableHelper.deleteFile(this, directory_logs.getPath());
                }
            });
        }
        if (directory_main.exists())
            RunnableHelper.runWriteCommand(project, new Runnable() {
                @Override
                public void run() {
                    RunnableHelper.deleteFile(this, directory_main.getPath());
                }
            });
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void exportInteractionLogs(Set<InteractionLogHandler> logs) {

        String dirPath = project.getBasePath() + "\\_behavioral_model_data\\logs";

        for (InteractionLogHandler logHandler : logs)
            if (!logHandler.getEventLog().getEvents().isEmpty()) {
                String[] temp = logHandler.getInteractionMethodName().split("\\.");
                String fileName = temp[temp.length - 2] + "-" + temp[temp.length - 1] + ".csv";
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

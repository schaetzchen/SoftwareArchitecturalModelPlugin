package plugindata.utils.component_identification;

import com.intellij.openapi.project.Project;
import plugindata.entities.ExecutionDataEvent;
import plugindata.entities.Graph;
import plugindata.RunnableHelper;
import plugindata.entities.SoftwareComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ComponentDataHandler {

    private static Set<String> packagesNames;
    private static Set<SoftwareComponent> components;

    public static ComponentExecutionData handleDataProjection(String componentName, List<ExecutionDataEvent> executionData) {

        ArrayList<ExecutionDataEvent> componentExecData = new ArrayList<>();

        for (ExecutionDataEvent event : executionData) {
            String calleePackageAndClassNames = event.getCalleeMethod().substring(0, event.getCalleeMethod().lastIndexOf('.'));
            if (calleePackageAndClassNames.equals(componentName))
                componentExecData.add(event);
            else {
                int index = calleePackageAndClassNames.lastIndexOf('.');
                if (index > 0 && calleePackageAndClassNames.substring(0, index).equals(componentName))
                    componentExecData.add(event);
            }
//            if (calleePackageAndClassNames.substring(0, calleePackageAndClassNames.lastIndexOf('.')).equals(componentName))
//                componentExecData.add(event);
        }

        return new ComponentExecutionData(componentName, componentExecData);
    }

    public static Set<SoftwareComponent> getComponents() {
        return components;
    }

    public static void retrievePackageDataInfo(Project project) {

        RunnableHelper.runReadCommand(project, () -> packagesNames = RunnableHelper.getPackageData(project));

        components = new HashSet<>();
        for (String s : packagesNames)
            components.add(new SoftwareComponent(s));
    }

    private static boolean checkMatrixEdge(ArrayList<ExecutionDataEvent> executionData, Integer o1, Integer o2) {

        for (ExecutionDataEvent event : executionData)
            if (event.getCallerID().intValue() == o1.intValue() && event.getCalleeID().intValue() == o2.intValue())
                return true;

        return false;
    }

    public static Set<ArrayList<Integer>> getComponentInstances(ArrayList<ExecutionDataEvent> executionData) {

        ArrayList<Integer> objects = new ArrayList<>();
        for (ExecutionDataEvent event : executionData)
            if (event.getCalleeID() != Integer.MIN_VALUE)
                objects.add(event.getCalleeID());

        Graph g = new Graph(objects.size());

        for (int i = 0; i < objects.size(); i++)
            for (int j = 0; j < objects.size(); j++)
                if (checkMatrixEdge(executionData, objects.get(i), objects.get(j))) {
                    g.addEdge(i, j);
                    g.addEdge(j, i);
                }

        Set<ArrayList<Integer>> res = new HashSet<>();

        for (ArrayList<Integer> instanceIndices : g.connectedComponents()) {
            ArrayList<Integer> instance = new ArrayList<>();
            for (Integer o : instanceIndices)
                instance.add(objects.get(o.intValue()));
            res.add(instance);
        }

        return res;
    }

    public static boolean checkIfSuchComponentExists(String str) {
        for (String comp : packagesNames)
            if (comp.equals(str))
                return true;
        return false;
    }
}

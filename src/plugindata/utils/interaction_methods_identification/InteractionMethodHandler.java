package plugindata.utils.interaction_methods_identification;

import plugindata.entities.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class InteractionMethodHandler {

    public static Graph getMethodCallingGraph(List<ExecutionDataEvent> executionData) {

        Graph g = new Graph(executionData.size());

        for (int i = 0; i < executionData.size() - 1; i++)
            for (int j = i + 1; j < executionData.size(); j++)
                if (
//                        !executionData.getData().get(i).getCalleeID().equals(Integer.MIN_VALUE) &&
//                executionData.getData().get(i).getCalleeID().equals(executionData.getData().get(j).getCallerID()))
                        executionData.get(i).getCalleeMethod().equals(executionData.get(j).getCallerMethod()))
                    g.addEdge(i, j);

        return g;
    }

    private static boolean checkInvocationRelation(int v1, int v2, Graph g) {

        return g.DFSUtil(v1, v2, new boolean[g.getNumberOfVertices()]);
    }

    public static SoftwareInterface getBelongingInterface(Set<SoftwareInterface> interfaces, String methodName) {

        for (SoftwareInterface anInterface : interfaces) {
            for (String s : anInterface.getMethodNames())
                if (methodName.equals(s))
                    return anInterface;
        }

        return null;
    }

    public static Set<SoftwareMethod> getInteractionMethods(Set<SoftwareInterface> interfaces,
                                                            SoftwareExecutionData data, Graph methodCallingGraph) {

        Set<SoftwareMethod> res = new HashSet<>();

        for (SoftwareInterface anInterface : interfaces) {
            for (String candidateMethod : anInterface.getMethodNames()) {

                boolean keepgoing = true;

                for (int i = 0; i < data.getData().size() - 1 && keepgoing; i++) {
                    for (int j = i; j < data.getData().size() && keepgoing; j++) {

                        if (candidateMethod.equals(data.getData().get(i).getCalleeMethod())) {
                            SoftwareInterface secondMethodInterface =
                                    getBelongingInterface(interfaces, data.getData().get(j).getCalleeMethod());

                            if (secondMethodInterface != null &&
                                    secondMethodInterface.getBelongingComponent().getName().equals(anInterface.getBelongingComponent().getName()) &&
                                    checkInvocationRelation(i, j, methodCallingGraph)) {
                                res.add(new SoftwareMethod(candidateMethod, anInterface));
                                keepgoing = false;
                            }
                        }
                    }
                }
            }
        }
        return res;
    }
}
package plugindata.utils.interface_identification;

import com.intellij.openapi.util.Pair;
import plugindata.entities.ExecutionDataEvent;
import plugindata.entities.Graph;
import plugindata.utils.component_identification.ComponentDataHandler;
import plugindata.utils.component_identification.ComponentExecutionData;
import plugindata.entities.SoftwareExecutionData;
import plugindata.entities.SoftwareInterface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class InterfaceDiscoveryHandler {

    public static Set<Pair<String, String>> getTopLevelMethodSetOfAComponent(ComponentExecutionData SD,
                                                                             SoftwareExecutionData executionData, Graph methodCallingGraph) {

        // First element in a pair is the name of a caller method, second is the name of a callee method
        Set<Pair<String, String>> topLevelMethods = new HashSet<>();

        for (ExecutionDataEvent event : SD.getExecutionData()) {
            String callerComponentName = getComponentNameForMethodCall(event.getCallerMethod());
//            String callerComponentName = event.getCallerMethod().substring(0, event.getCallerMethod().lastIndexOf('.'));
//            callerComponentName = callerComponentName.substring(0, callerComponentName.lastIndexOf('.'));
            if (!callerComponentName.equals(SD.getComponentName()))
                topLevelMethods.add(new Pair<>(event.getCallerMethod(), event.getCalleeMethod()));
            else {
                for (int i = 0; i < executionData.getData().size(); i++) {
                    ExecutionDataEvent eventCopy = executionData.getData().get(i);
                    if (event.getCalleeMethod().equals(eventCopy.getCalleeMethod()) &&
                            event.getCallerMethod().equals(eventCopy.getCallerMethod()) &&
                            event.getCalleeID().equals(eventCopy.getCalleeID()) &&
                            event.getCallerID().equals(eventCopy.getCallerID())) {

                        for (Integer y : methodCallingGraph.getAdjacentVertices(i))
                            if (ComponentDataHandler.checkIfSuchComponentExists(getComponentNameForMethodCall(executionData.getData().get(y).getCalleeMethod())) &&
                                    ComponentDataHandler.checkIfSuchComponentExists(getComponentNameForConstructorCall(executionData.getData().get(y).getCalleeMethod())) &&
                                    !getComponentNameForMethodCall(executionData.getData().get(y).getCalleeMethod())
                                            .equals(SD.getComponentName()))
//                                    !getComponentNameForConstructorCall(executionData.getData().get(y).getCalleeMethod())
//                        .equals(SD.getComponentName()) )
//                                    || !getComponentNameForMethodCall(executionData.getData().get(y).getCallerMethod())
//                                            .equals(getComponentNameForConstructorCall(executionData.getData().get(y).getCalleeMethod())) )
                                topLevelMethods.add(new Pair<>(event.getCallerMethod(), event.getCalleeMethod()));
//                                topLevelMethods.add(new Pair<>(executionData.getData().get(y).getCallerMethod(),
//                                        executionData.getData().get(y).getCalleeMethod()));
                    }
                }
            }
        }

        return topLevelMethods;
    }

    private static String getComponentNameForMethodCall(String methodSignature) {
        String methodName = methodSignature.substring(methodSignature.lastIndexOf('.') + 1);
        if (methodName.length() <= 1)
            return "-";
        if (methodName.charAt(0) != methodName.toLowerCase().charAt(0))
            return getComponentNameForConstructorCall(methodSignature);

        String res = methodSignature.substring(0, methodSignature.lastIndexOf('.'));
        if (res.lastIndexOf('.') > 0)
            return res.substring(0, res.lastIndexOf('.'));
        else
            return "-";
    }

    private static String getComponentNameForConstructorCall(String constructorName) {
        return constructorName.substring(0, constructorName.lastIndexOf('.'));
    }

    public static Set<Set<String>> getCandidateInterfaceSet(Set<Pair<String, String>> topLevelMethodSet) {

        Set<Pair<String, Set<String>>> tempSet = new HashSet<>();

        for (Pair<String, String> p : topLevelMethodSet)
            handleSetOfPairs(tempSet, p);

        Set<Set<String>> res = new HashSet<>();
        for (Pair<String, Set<String>> p : tempSet)
            res.add(p.second);

        return res;
    }

    private static Double getCandidateInterfaceSimilarity(Set<String> first, Set<String> second) {

        Integer numberOfCommonMethods = 0;
        for (String s1 : first)
            for (String s2 : second)
                if (s1.equals(s2))
                    numberOfCommonMethods++;

        return numberOfCommonMethods.doubleValue() / (first.size() + second.size());
    }

    public static List<Set<String>> mergeInterfaces(Set<Set<String>> candidates, Double threshold) {

        if (candidates.isEmpty())
            return new ArrayList<>();

        Double similarity = 1.;

        ArrayList<Set<String>> list = new ArrayList<>();
        list.addAll(candidates);
        if (list.size() == 1)
            return list;

        while (similarity >= threshold) {

            boolean keepgoing = true;
            for (int i = 0; i < list.size() - 1 && keepgoing; i++)
                for (int j = i + 1; j < list.size() && keepgoing; j++) {

                    similarity = getCandidateInterfaceSimilarity(list.get(i), list.get(j));
                    if (similarity >= threshold) {

                        list.add(mergeTwoCandidates(list.get(i), list.get(j)));
                        list.remove(j);
                        list.remove(i);
                        keepgoing = false;
                    }
                }
        }

        return list;
    }

    private static Set<String> mergeTwoCandidates(Set<String> first, Set<String> second) {

        Set<String> res = new HashSet<>();
        res.addAll(first);
        res.addAll(second);
        return res;
    }

    private static void handleSetOfPairs(Set<Pair<String, Set<String>>> set, Pair<String, String> newPair) {

        for (Pair<String, Set<String>> el : set)
            if (el.first.equals(newPair.first)) {
                el.second.add(newPair.second);
                return;
            }

        Set<String> newSet = new HashSet<>();
        newSet.add(newPair.second);
        set.add(new Pair<>(newPair.first, newSet));
    }

    public Set<ExecutionDataEvent> getInterfaceExecutionData(SoftwareInterface anInterface, SoftwareExecutionData data) {

        Set<ExecutionDataEvent> res = new HashSet<>();

        for (ExecutionDataEvent event : data.getData()) {

            if (anInterface.containsMethod(event.getCalleeMethod()) &&
                    !event.getCallerMethod().substring(0, event.getCallerMethod().lastIndexOf(".")).
                            equals(anInterface.getBelongingComponent().getName()))
                res.add(event);
        }

        return res;
    }
}
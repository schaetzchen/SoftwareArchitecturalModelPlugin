package plugindata.utils.interaction_methods_identification;

import plugindata.entities.*;

import java.util.*;

public class InteractionLogHandler {

    private SoftwareMethod interactionMethod;
    private Log eventLog;
    private Set<Integer> softwareRuns;
    private Set<List<ExecutionDataEvent>> runMethodCalls;

    public InteractionLogHandler(SoftwareMethod interactionMethod, SoftwareExecutionData executionData, Set<SoftwareInterface> interfaces) {
        this.interactionMethod = interactionMethod;
        eventLog = new Log();
        softwareRuns = new HashSet<>();
        initSoftwareRuns(executionData);
        initRunMethodCalls(executionData);
        createLog(interfaces);
    }

    private void createLog(Set<SoftwareInterface> interfaces) {

        for (List<ExecutionDataEvent> events : runMethodCalls) {
            Graph g = InteractionMethodHandler.getMethodCallingGraph(events);
            for (SoftwareInterface softwareInterface : interfaces)
                handleInterface(softwareInterface, events, g);
        }
    }

    private void handleInterface(SoftwareInterface softwareInterface, List<ExecutionDataEvent> events, Graph g) {

        for (int i = 0; i < events.size() - 1; i++)
            for (int j = 1; j < events.size(); j++)
                if (events.get(i).getCalleeMethod().equals(interactionMethod.getMethodName()) &&
                        softwareInterface.containsMethod(events.get(j).getCalleeMethod()) &&
                        g.DFSUtil(i, j, new boolean[g.getNumberOfVertices()]))
                    addToLog(softwareInterface, events, g);
//        for (ExecutionDataEvent event : events)
//            if (event.getCallerMethod().equals(interactionMethod.getMethodName()))
//                if (softwareInterface.containsMethod(event.getCalleeMethod())) {
//                    addToLog(softwareInterface, events);
//                    return;
//                }
    }

    private void addToLog(SoftwareInterface softwareInterface, List<ExecutionDataEvent> events, Graph g) {

        long day = 1000 * 60 * 60 * 24;
        Date startTime = new Date(Long.MAX_VALUE);
        Date completeTime = new Date(-day);
        Integer caseID = Integer.MIN_VALUE;

        for (int i = 0; i < events.size() - 1; i++)
            for (int j = 1; j < events.size(); j++)
                if (events.get(i).getCalleeMethod().equals(interactionMethod.getMethodName()) &&
                        softwareInterface.containsMethod(events.get(j).getCalleeMethod()) &&
                        g.DFSUtil(i, j, new boolean[g.getNumberOfVertices()])) {

                    ExecutionDataEvent event = events.get(i);
                    caseID = event.getCaseID();

                    if (event.getDate().before(startTime))
                        startTime = event.getDate();
                    if (event.getDate().after(completeTime))
                        completeTime = event.getDate();
                }

        eventLog.addEvent(new LogEvent(caseID, softwareInterface.getName(), "start", startTime));
        eventLog.addEvent(new LogEvent(caseID, softwareInterface.getName(), "complete", completeTime));
    }

    private void initSoftwareRuns(SoftwareExecutionData executionData) {

        for (ExecutionDataEvent event : executionData.getData())
            if (event.getCalleeMethod().equals(interactionMethod.getMethodName()))
                softwareRuns.add(event.getCaseID());
    }

    private void initRunMethodCalls(SoftwareExecutionData executionData) {

        runMethodCalls = new HashSet<>();

        for (Integer runID : softwareRuns) {
            List<ExecutionDataEvent> runEvents = new ArrayList<>();
            for (ExecutionDataEvent event : executionData.getData()) {
                if (event.getCaseID().equals(runID))
                    runEvents.add(event);
            }
            runMethodCalls.add(runEvents);
        }
    }

    public Log getEventLog() {
        return eventLog;
    }

    public String getInteractionMethodName() {
        return interactionMethod.getMethodName();
    }
}

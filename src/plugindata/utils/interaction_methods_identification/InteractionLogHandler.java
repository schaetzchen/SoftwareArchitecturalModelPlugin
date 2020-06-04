package plugindata.utils.interaction_methods_identification;

import plugindata.entities.*;

import java.util.*;

public class InteractionLogHandler {

    private SoftwareMethod interactionMethod;
    private Log eventLog;
    private Set<Integer> softwareRuns;
    private Set<Set<ExecutionDataEvent>> runMethodCalls;

    public InteractionLogHandler(SoftwareMethod interactionMethod, SoftwareExecutionData executionData, Set<SoftwareInterface> interfaces) {
        this.interactionMethod = interactionMethod;
        eventLog = new Log();
        softwareRuns = new HashSet<>();
        initSoftwareRuns(executionData);
        initRunMethodCalls(executionData);
        createLog(interfaces);
    }

    private void createLog(Set<SoftwareInterface> interfaces) {

        for (Set<ExecutionDataEvent> events : runMethodCalls)
            for (SoftwareInterface softwareInterface : interfaces)
                handleInterface(softwareInterface, events);
    }

    private void handleInterface(SoftwareInterface softwareInterface, Set<ExecutionDataEvent> events) {

        for (ExecutionDataEvent event : events)
            if (event.getCallerMethod().equals(interactionMethod.getMethodName()) &&
                    softwareInterface.containsMethod(event.getCalleeMethod())) {
                addToLog(softwareInterface, events);
                return;
            }
    }

    private void addToLog(SoftwareInterface softwareInterface, Set<ExecutionDataEvent> events) {

        long day = 1000 * 60 * 60 * 24;
        Date startTime = new Date(Long.MAX_VALUE);
        Date completeTime = new Date(-day);
        Integer caseID = Integer.MIN_VALUE;

        for (ExecutionDataEvent event : events)
            if (softwareInterface.containsMethod(event.getCalleeMethod())) {
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
            Set<ExecutionDataEvent> runEvents = new HashSet<>();
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

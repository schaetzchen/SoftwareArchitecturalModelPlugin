package plugindata.entities;

import java.util.HashSet;
import java.util.Set;

public class Log {

    Set<LogEvent> events;

    public Log() {
        events = new HashSet<>();
    }

    public Set<LogEvent> getEvents() {
        return events;
    }

    public void addEvent(LogEvent event) {
        events.add(event);
    }
}

package plugindata.entities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEvent {

    private Integer caseID;
    private String activityName, transactionType;
    private Date timestamp;

    public LogEvent(Integer caseID, String activityName, String transactionType, Date timestamp) {
        this.caseID = caseID;
        this.activityName = activityName;
        this.transactionType = transactionType;
        this.timestamp = timestamp;
    }

    public Integer getCaseID() {
        return caseID;
    }

    public String getActivityName() {
        return activityName;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getEventData() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return caseID + "," + activityName + "," + transactionType + "," + dateFormat.format(timestamp);
    }
}

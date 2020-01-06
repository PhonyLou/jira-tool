package jiratool.beans.history;

public class HistoryDetail {
    private String fieldId;
    private Long timestamp;
    private Status from;
    private Status to;

    @Override
    public String toString() {
        return "HistoryDetail{" +
                "fieldId='" + fieldId + '\'' +
                ", timestamp=" + timestamp +
                ", from=" + from +
                ", to=" + to +
                '}';
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Status getFrom() {
        return from;
    }

    public void setFrom(Status from) {
        this.from = from;
    }

    public Status getTo() {
        return to;
    }

    public void setTo(Status to) {
        this.to = to;
    }
}

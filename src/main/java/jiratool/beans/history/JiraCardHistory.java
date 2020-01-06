package jiratool.beans.history;

import java.util.List;

public class JiraCardHistory {
    List<HistoryDetail> items;

    public List<HistoryDetail> getItems() {
        return items;
    }

    public void setItems(List<HistoryDetail> items) {
        this.items = items;
    }
}

package jiratool.beans.card;

import java.util.List;

public class JiraCards {
    private int maxResults;
    private int total;
    private List<JiraCard> issues;

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<JiraCard> getIssues() {
        return issues;
    }

    public void setIssues(List<JiraCard> issues) {
        this.issues = issues;
    }
}

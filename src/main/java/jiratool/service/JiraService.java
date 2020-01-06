package jiratool.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jiratool.beans.history.HistoryDetail;
import jiratool.beans.history.JiraCardHistory;
import jiratool.beans.card.JiraCards;
import jiratool.util.TimeTool;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class JiraService {
    public static Map<String, Double> getCardCycleTime(final String jiraId, final String jiraToken) throws UnirestException, IOException {
        Map<String, Float> result = new HashMap<>();
        Map<String, Double> finalResult = new HashMap<>();

        HttpResponse<JsonNode> response = Unirest.get("https://arlive.atlassian.net/rest/internal/2/issue/"+jiraId+"/activityfeed")
                .header("Accept", "application/json")
                .header("Authorization", jiraToken)
                .asJson();

        JsonNode jsonNode = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        JiraCardHistory jiraCardHistory = objectMapper.readValue(jsonNode.toString(), JiraCardHistory.class);

        List<HistoryDetail> activities = jiraCardHistory.getItems().stream()
                .filter(x -> "status".equals(x.getFieldId()))
                .collect(Collectors.toList());
        for (int index = 0; index < activities.size(); index++) {
            HistoryDetail nextActivity = null;
            if (index < activities.size() - 1) {
                nextActivity = activities.get(index + 1);
            }

            HistoryDetail currentActivity = activities.get(index);
            Float costHour = result.get(currentActivity.getTo().getDisplayValue());

            if (null == nextActivity) {
                if (costHour != null) {
                    costHour += TimeTool.getWorkDay(currentActivity.getTimestamp(), System.currentTimeMillis());
                } else {
                    costHour = TimeTool.getWorkDay(currentActivity.getTimestamp(), System.currentTimeMillis());
                }
            } else {
                if (costHour != null) {
                    costHour += TimeTool.getWorkDay(currentActivity.getTimestamp(), nextActivity.getTimestamp());
                } else {
                    costHour = TimeTool.getWorkDay(currentActivity.getTimestamp(), nextActivity.getTimestamp());
                }
            }
            result.put(currentActivity.getTo().getDisplayValue(), costHour);
        }

        result.forEach((key, value) -> finalResult.put(key, TimeTool.roundUp(value, 1)));

        return finalResult;
    }

    public static JiraCards getCards(final String jql, final String jiraToken) throws UnirestException, IOException {

        HttpResponse<JsonNode> response = Unirest.get("https://arlive.atlassian.net/rest/api/2/search")
                .queryString("jql", jql)
                .header("Accept", "application/json")
                .header("Authorization", jiraToken)
                .asJson();
        JsonNode jsonNode = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper.readValue(jsonNode.toString(), JiraCards.class);
    }

    public static CompletableFuture<Map<String, Double>> getCycleTime(final String jiraId, final String jiraToken) throws IOException, UnirestException {
        Map<String, Double> result = getCardCycleTime(jiraId, jiraToken);
        return CompletableFuture.completedFuture(result);
    }
}

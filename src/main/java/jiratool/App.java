package jiratool;


import com.mashape.unirest.http.exceptions.UnirestException;
import com.qcloud.scf.runtime.Context;
import com.qcloud.services.scf.runtime.events.APIGatewayProxyRequestEvent;
import jiratool.beans.card.JiraCards;
import jiratool.beans.cycletime.CycleTimeBean;
import jiratool.service.FileService;
import jiratool.service.JiraService;
import jiratool.util.Converter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class App {

    public String mainHandler(APIGatewayProxyRequestEvent requestEvent, Context context) throws UnirestException, IOException {
        System.out.println("start main handler");
        System.out.println("requestEvent: " + requestEvent);
        System.out.println("context: " + context);

        String responseStr;
        switch (requestEvent.getPath()) {
            case "/jira/cardCycleTime":
                responseStr = generateNormalResponse(getCardCycleTime(requestEvent.getHeaders()));
                break;
            case "/jira/getCardsFile":
                responseStr = generateNormalResponse(getCardsFile(requestEvent.getHeaders()));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + requestEvent.getPath());
        }

        return responseStr;
    }

    private static <T> String generateNormalResponse(T data) {
        String responseStr = "";
        if (data instanceof Map) {
            responseStr = Converter.convertMap((Map<String, Double>) data);
        } else {
            responseStr = (String) data;
        }
        return responseStr;
    }

    private Map<String, Double> getCardCycleTime(Map<String, String> header) throws UnirestException, IOException {
        final String jiraToken = header.get("jira-token");
        final String jiraId = header.get("jira-id");

        return JiraService.getCardCycleTime(jiraId, jiraToken);
    }

    private String getCardsFile(Map<String, String> header) throws IOException, UnirestException {
        final String jiraToken = header.get("jira-token");
        final String jql = header.get("jql");
        final List<String> cardStages = new ArrayList<>(Arrays.asList(header.get("card-stage").split(",")))
                .stream().map(String::trim).collect(Collectors.toList());

        JiraCards jiraCards = enrichCardDetail(JiraService.getCards(jql, jiraToken), jiraToken);
        return FileService.generateFile(jiraCards, cardStages);
    }

    private JiraCards enrichCardDetail(final JiraCards jiraCards, final String jiraToken) {
        final Map<String, CompletableFuture<Map<String, Double>>> cycleTimeMap = new HashMap<>();

        jiraCards.getIssues().forEach(card -> {
            final CompletableFuture<Map<String, Double>> futureCycleTime;
            try {
                futureCycleTime = JiraService.getCycleTime(card.getKey(), jiraToken);
                cycleTimeMap.put(card.getKey(), futureCycleTime);
            } catch (IOException | UnirestException e) {
                e.printStackTrace();
            }
        });

        jiraCards.getIssues().forEach(card -> {
            Map<String, Double> cycleTime = null;
            try {
                cycleTime = cycleTimeMap.get(card.getKey()).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            CycleTimeBean cycleTimeBean = new CycleTimeBean();
            cycleTimeBean.setCycleTime(cycleTime);
            card.getFields().setCycleTimeBean(cycleTimeBean);
        });

        return jiraCards;
    }

    public static void main(String[] args) {
        App app = new App();
        Map<String, String> header = new HashMap<>();
        header.put("jira-token", "Your token");
        header.put("jira-id", "JiraId");
        header.put("jql", "jira query language");
        header.put("card-stage", "READY FOR DEV, In Progress, Blocked, Showcase, Done");
        try {
//            Map<String, Double> res = app.getCardCycleTime(header);
            String res = app.getCardsFile(header);
            System.out.println(res);
        } catch (UnirestException | IOException e) {
            e.printStackTrace();
        }
    }
}

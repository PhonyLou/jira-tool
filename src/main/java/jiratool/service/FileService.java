package jiratool.service;

import jiratool.beans.card.JiraCard;
import jiratool.beans.card.JiraCards;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileService {

    public static String generateFile(JiraCards jiraCards, final List<String> cardStages) {

        final StringBuffer contentBuffer = new StringBuffer();
        final StringBuffer stageHeader = new StringBuffer();
        cardStages.forEach(stage -> stageHeader.append(",").append(stage));
        final String tableHeader = "jiraId,Issue Type,Status,Summary,Priority,Assignee,Reporter" + stageHeader.toString();
        contentBuffer.append(tableHeader);
        contentBuffer.append("\r\n");

        jiraCards.getIssues().forEach(jiraCard -> {
            contentBuffer
                    .append(jiraCard.getKey()).append(",")
                    .append(jiraCard.getFields().getIssuetype().getName()).append(",")
                    .append(jiraCard.getFields().getStatus().getStatusCategory().getName()).append(",")
                    .append(jiraCard.getFields().getSummary().replaceAll(",", " ")).append(",")
                    .append(jiraCard.getFields().getPriority().getName()).append(",")
                    .append(jiraCard.getFields().getAssignee().getName()).append(",")
                    .append(jiraCard.getFields().getReporter().getName());
            cardStages.forEach(stage -> {
                final String stageCostStr = getLeadTimes(jiraCard, stage);
                contentBuffer.append(",").append(stageCostStr);
            });
            contentBuffer.append("\r\n");
        });

        return contentBuffer.toString();
    }

    public static String generateCycleTime(JiraCards jiraCards, final List<String> cardStages) {

        final StringBuffer contentBuffer = new StringBuffer();
        contentBuffer.append("[");

        jiraCards.getIssues().forEach(jiraCard -> {
            contentBuffer
                    .append("{")
                    .append("'jiraId'")
                    .append(":").append("'").append(jiraCard.getKey()).append("'");


            final List<Double> cycleTimeList = new ArrayList<>();
            final List<Double> wipTime = new ArrayList<>();
            cardStages.forEach(stage -> {
                if(!"done".equalsIgnoreCase(stage)) {
                    final String stageCostStr = getLeadTimes(jiraCard, stage);
                    final double stageCost;
                    if ("-".equals(stageCostStr)) {
                        stageCost = 0d;
                    } else {
                        stageCost = Double.parseDouble(stageCostStr);
                    }

                    cycleTimeList.add(stageCost);
                    if (stage.contains("Progress")) {
                        wipTime.add(stageCost);
                    }
                }
            });

            final double totalCost = cycleTimeList.stream().mapToDouble(Double::doubleValue).sum();
            final double wipCost = wipTime.stream().mapToDouble(Double::doubleValue).sum();
            DecimalFormat df = new DecimalFormat("#.##");

            contentBuffer
                    .append(",")
                    .append("'totalCost'")
                    .append(":")
                    .append(df.format(totalCost));

            contentBuffer
                    .append(",")
                    .append("'wipCost'")
                    .append(":")
                    .append(df.format(wipCost));
            contentBuffer.append("},");
        });

        contentBuffer.append("]");

        return contentBuffer.toString();
    }

    private static String getLeadTimes(JiraCard jiraCard, String stageName) {
        Double stageTime = jiraCard.getFields().getCycleTimeBean().getCycleTime().get(stageName);
        String leadHours;
        if (null != stageTime) {
            leadHours = String.valueOf(stageTime);
        } else {
            leadHours = "-";
        }
        return leadHours;
    }

}

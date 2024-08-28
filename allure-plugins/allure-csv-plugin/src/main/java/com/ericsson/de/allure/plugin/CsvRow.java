package com.ericsson.de.allure.plugin;

import java.io.Serializable;
import java.util.List;

import com.google.common.annotations.VisibleForTesting;
import ru.yandex.qatools.allure.data.Time;

/**
 * Class acts as holder for data to be exported by CSV plugin
 *
 */
public class CsvRow implements Serializable {
    private static final String SEPARATOR = ";";
    public static final CsvRow HEADER = initHeader();

    private String title = "";
    private String component = "";
    private String result = "";
    private String group = "";
    private String requirement = "";
    private String defect = "";
    private String comment = "";
    private String executionType = "";
    private String priority = "";
    private String sprint = "";
    private String suite = "";
    private String nodeName ="";
    private String nodeType ="";
    private String duration ="";
    private String teamName ="";
    private Time time;


    public String getNodeName() { return nodeName; }

    public String getNodeType() { return nodeType; }

    public String getTitle() {
        return title;
    }

    public String getComponent() {
        return component;
    }

    public String getResult() {
        return result;
    }

    public String getGroup() {
        return group;
    }

    public String getRequirement() {
        return requirement;
    }

    public String getDefect() {
        return defect;
    }

    public String getComment() {
        return comment;
    }

    public String getExecutionType() {
        return executionType;
    }

    public String getPriority() {
        return priority;
    }

    public String getSprint() {
        return sprint;
    }

    public String getSuite() {
        return suite;
    }

    public Time getTime() { return this.time; }

    public String getDuration(){ return duration; }

    public void setNodeName(String nodeName) { this.nodeName = nodeName; }

    public void setNodeType(String nodeType) { this.nodeType = nodeType; }

    public void setDuration(String duration) { this.duration = duration; }

    public void setResult(String result) {
        this.result = result;
    }

    public void setTime(Time time) { this.time=time; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public void setDefect(String defect) {
        this.defect = defect;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public void setComment(List<String> commentLabels) {
        this.comment = getJoinedValue(commentLabels);
    }

    public void setExecutionType(List<String> executionTypeLabels) {
        this.executionType = getJoinedValue(executionTypeLabels);
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public void setGroup(List<String> groupLabels) {
        this.group = getJoinedValue(groupLabels);
    }

    public void setComponent(List<String> componentLabels) {
        this.component = getJoinedValue(componentLabels);
    }

    public void setDefect(List<String> defectLabels) {
        this.defect = getJoinedValue(defectLabels);
    }

    public void setRequirement(List<String> requirementLabels) {
        this.requirement = getJoinedValue(requirementLabels);
    }

    public void setSprint(String sprint) {
        this.sprint = sprint;
    }

    public void setSprint(List<String> sprints) {
        this.sprint = getJoinedValue(sprints);
    }

    public void setSuite(String suite) {
        this.suite = suite;
    }

    public String getTeamName() {  return teamName;    }

    public void setTeamName(String teamName) { this.teamName = teamName;  }

    @VisibleForTesting
    static String getJoinedValue(List<String> items) {
        StringBuilder valueBuilder = new StringBuilder();
        if (items != null) {
            for (String item : items) {
                joinItem(item, valueBuilder);
            }
        }
        return valueBuilder.toString();
    }

    private static void joinItem(String item, StringBuilder valueBuilder) {
        if (item != null && !item.isEmpty()) {
            if (valueBuilder.length() == 0) {
                valueBuilder.append(item);
            } else {
                valueBuilder.append(SEPARATOR).append(item);
            }
        }
    }

    private static CsvRow initHeader() {
        CsvRow row = new CsvRow();
        row.setTitle("Title");
        row.setComponent("Component");
        row.setComment("Comment");
        row.setDefect("Defect");
        row.setExecutionType("Execution type");
        row.setPriority("Priority");
        row.setRequirement("Requirement");
        row.setResult("Result");
        row.setGroup("Group");
        row.setSprint("Sprint");
        row.setSuite("Suite");
        row.setNodeName("Node Name");
        row.setNodeType("Node Type");
        row.setDuration("Duration");
        row.setTeamName("Team Name");
        return row;
    }
}

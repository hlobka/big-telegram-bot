package atlassian.jira;

import lombok.Data;

import java.util.Date;

@Data
public class SprintDto {
    private final Long id;
    private final Long rapidViewId;
    private final String state;
    private final String name;
    private final String startDateString;
    private final Date startDate;
    private final String endDateString;
    private final Date endDate;
    private final String completeDateString;
    private final Date completeDate;
    private final Long sequence;
    private final String goal;
}

package atlassian.jira.subclient;

import lombok.Data;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.net.URI;

@Data
public class VersionDto {
    private final URI self;
    @Nullable
    private final Long id;
    private final String description;
    private final String name;
    private final boolean isArchived;
    private final boolean isReleased;
    @Nullable
    private final DateTime startDate;
    @Nullable
    private final DateTime releaseDate;
}

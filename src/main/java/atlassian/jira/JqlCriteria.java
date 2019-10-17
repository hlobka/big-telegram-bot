package atlassian.jira;

import lombok.Data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
public class JqlCriteria {
    Integer maxPerQuery = 100;
    Integer startIndex = 0;
    Set<String> fields = null;

    public JqlCriteria withMaxPerQuery(Integer value) {
        this.maxPerQuery = value;
        return this;
    }

    public JqlCriteria withStartIndex(Integer value) {
        this.startIndex = value;
        return this;
    }

    public JqlCriteria withFillInformation(Boolean value) {
        if (value) {
            this.fields = new HashSet<>(Collections.singletonList("*all"));
        } else {
            if (this.fields != null) {
                this.fields.remove("*all");
            }
        }
        return this;
    }

    public JqlCriteria withFields(Set<String> value) {
        this.fields = value;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        JqlCriteria that = (JqlCriteria) o;

        if (!Objects.equals(maxPerQuery, that.maxPerQuery)) {
            return false;
        }
        if (!Objects.equals(startIndex, that.startIndex)) {
            return false;
        }
        return Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
        int result = maxPerQuery != null ? maxPerQuery.hashCode() : 0;
        result = 31 * result + (startIndex != null ? startIndex.hashCode() : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }
}

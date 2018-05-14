package upsource.filter;

import java.util.function.Function;
import java.util.function.Predicate;

public enum CountCondition {
    EQUALS(Object::equals),
    MORE_THAN((t, e)-> t.doubleValue() > e.doubleValue()),
    LESS_THAN((t, e)-> t.doubleValue() < e.doubleValue()),
    MORE_THAN_OR_EQUALS((t, e)-> t.doubleValue() >= e.doubleValue()),
    LESS_THAN_OR_EQUALS((t, e)-> t.doubleValue() <= e.doubleValue());

    private ConditionChecker<Number, Number> conditionChecker;

    CountCondition(ConditionChecker<Number, Number> conditionChecker) {
        this.conditionChecker = conditionChecker;
    }

    public <T> Predicate<T> getChecker(Class<T> tClass, Function<T, Number> function, Number actual) {
        return review -> conditionChecker.check(function.apply(review), actual);
    }
}

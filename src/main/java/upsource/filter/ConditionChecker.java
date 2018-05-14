package upsource.filter;
@FunctionalInterface
public interface ConditionChecker<T, E> {
    boolean check(T t, E e);
}

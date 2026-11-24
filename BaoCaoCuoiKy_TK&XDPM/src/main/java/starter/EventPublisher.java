package starter;

public interface EventPublisher {
    void publish(DomainEvent event);
    boolean hasPublished(Class<? extends DomainEvent> eventClass);
    void clear();
}
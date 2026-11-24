package starter;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventPublisherMock implements EventPublisher {
    private final List<DomainEvent> events = new ArrayList<>();

    @Override
    public void publish(DomainEvent event) {
        if (event != null) {
            events.add(event);
        }
    }

    @Override
    public boolean hasPublished(Class<? extends DomainEvent> eventClass) {
        return events.stream().anyMatch(e -> eventClass.isAssignableFrom(e.getClass()));
    }

    @Override
    public void clear() {
        events.clear();
    }

    public List<DomainEvent> getEvents() {
        return events;
    }
}
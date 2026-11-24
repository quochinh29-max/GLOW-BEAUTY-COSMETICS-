package starter;

import java.time.LocalDateTime;

public interface DomainEvent {
    LocalDateTime occurredOn();
}
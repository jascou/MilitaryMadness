package military.engine.events;

import military.engine.Team;

public class TurnStarted implements GameEvent {
    public final Team team;
    public TurnStarted(Team team) {
        this.team = team;
    }
}

package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;

import java.util.ArrayList;
import java.util.List;

public final class GameState {
    private final Level level;
    private final Snake snake;
    private final List<Pin> pins;
    private final Status status;
    private final Direction pendingDirection;

    public GameState(
        Level level, Snake snake, List<Pin> pins, Status status, Direction pendingDirection) {
        // TODO: lege einen neuen GameState mit den übergebenen Informationen an
        this.level = level;
        this.snake = snake;
        this.pins = pins != null ? List.copyOf(pins) : List.of();
        this.status = status;
        this.pendingDirection = pendingDirection;
    }

  public Level level() {
    // TODO: Getter
      return this.level;
  }

  public Snake snake() {
    // TODO: Getter
      return this.snake;
  }

  public List<Pin> pins() {
    // TODO: Getter
      return this.pins;
  }

  public Status status() {
    // TODO: Getter
      return this.status;
  }

  public Direction pendingDirection() {
    // TODO: Getter
      return this.pendingDirection;
  }

    public GameState tick() {
        // 1. Early Exit: Keine Bewegung ohne Richtung oder wenn Spiel vorbei
        if (!this.status.isRunning() || this.pendingDirection == null || this.pendingDirection == Direction.NONE) {
            return this;
        }

        Position nextHeadPos = this.snake.nextHead(this.pendingDirection);

        // 2. Spielfeld verlassen -> Verloren
        if (!this.level.isInside(nextHeadPos)) {
            return new GameState(this.level, this.snake, this.pins, Status.LOST_OUT_OF_BOUNDS, this.pendingDirection);
        }

        // 3. Wand berührt -> Blockiert (Richtung wird auf NONE gesetzt)
        if (this.level.cellAt(nextHeadPos) == CellType.WALL) {
            return new GameState(this.level, this.snake, this.pins, this.status, Direction.NONE);
        }

        // 4. Selbstkollision -> Verloren
        if (this.snake.occupies(nextHeadPos)) {
            return new GameState(this.level, this.snake, this.pins, Status.LOST_SELF_COLLISION, this.pendingDirection);
        }

        // 5. Pin-Logik prüfen
        int pinIndex = -1;
        for (int i = 0; i < this.pins.size(); i++) {
            if (this.pins.get(i).position().equals(nextHeadPos)) {
                pinIndex = i;
                break;
            }
        }

        // Wenn ein Pin auf dem nächsten Feld liegt...
        if (pinIndex != -1) {
            Pin targetedPin = this.pins.get(pinIndex);

            // (d) Pin ist schon HIGH ODER wir kommen aus der falschen Richtung -> Blockieren!
            if (targetedPin.state().isSet() || targetedPin.activationDirection() != this.pendingDirection) {
                return new GameState(this.level, this.snake, this.pins, this.status, Direction.NONE);
            }

            // Pin erfolgreich knacken!
            List<Pin> updatedPins = new ArrayList<>(this.pins);
            updatedPins.set(pinIndex, targetedPin.withState(Pin.State.HIGH));

            // Prüfen, ob alle Pins geknackt sind
            Status nextStatus = updatedPins.stream().allMatch(pin -> pin.state().isSet()) ? Status.WON : Status.RUNNING;

            // Zustand zurückgeben. Wichtig: Die Schlange zieht NICHT auf das Pin-Feld, sondern bleibt davor stehen (Direction.NONE)
            return new GameState(this.level, this.snake, updatedPins, nextStatus, Direction.NONE);
        }

        // 6. Reguläre Bewegung: Die Schlange wächst als Dietrich in die gewünschte Richtung
        Snake movedSnake = this.snake.grow(this.pendingDirection);
        return new GameState(this.level, movedSnake, this.pins, this.status, this.pendingDirection);
    }
  public enum Status {
    RUNNING,
    WON,
    LOST_SELF_COLLISION,
    LOST_OUT_OF_BOUNDS;

    public boolean isRunning() {
      return this == RUNNING;
    }
  }
}

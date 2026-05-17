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
    // TODO: diese Methode lässt das Spiel einen Schritt laufen (berechnet den Spielzustand im
    // nächsten Schritt)

    // TODO: early exit: wenn das Spiel nicht läuft oder keine Blickrichtung gesetzt ist: keine
      if (!this.status.isRunning() || this.pendingDirection == null || this.pendingDirection == Direction.NONE) {
          return this;
      }
      Position nextHeadPos = this.snake.nextHead(this.pendingDirection);

    // TODO: prüfe die folgenden Bedingungen:
    // (a) Schlange würde das Spielfeld verlassen: Spiel verloren
      if (!this.level.isInside(nextHeadPos)) {
          return new GameState(this.level, this.snake, this.pins, Status.LOST_OUT_OF_BOUNDS, this.pendingDirection);
      }
    // (b) Schlange würde in ein Wandelement gehen: Blockiert (keine Bewegung, Blickrichtung "none")
      if (this.level.cellAt(nextHeadPos) == CellType.WALL) {
          return new GameState(this.level, this.snake, this.pins, this.status, Direction.NONE);
      }
    // (c) Schlange beisst sich: Spiel verloren
      if (this.snake.occupies(nextHeadPos)) {
          return new GameState(this.level, this.snake, this.pins, Status.LOST_SELF_COLLISION, this.pendingDirection);
      }
      int pinIndex = -1;
      for (int i = 0; i < this.pins.size(); i++) {
          if (this.pins.get(i).position().equals(nextHeadPos)) {
              pinIndex = i;
              break;
          }
      }

      if (pinIndex != -1) {
          Pin targetedPin = this.pins.get(pinIndex);
    // (d) Schlange würde auf einen Pin gehen (Pin bereits gesetzt oder Schlange kommt nicht in der
          if (targetedPin.state().isSet() || targetedPin.activationDirection() != this.pendingDirection) {
              return new GameState(this.level, this.snake, this.pins, this.status, Direction.NONE);
          }
    // Aktivierungsrichtung: Blockiert (keine Bewegung, Blickrichtung "none")

    // TODO: aktiviere einen noch nicht gesetzten Pin, wenn die Schlange in der richtigen Richtung
    // auf den Pin gehen würde (die Schlange darf dabei aber nicht auf den Pin gehen)
          if (!targetedPin.state().isSet() && targetedPin.activationDirection() == this.pendingDirection) {
              // Pin-Liste kopieren und den betroffenen Pin auf HIGH setzen
              List<Pin> updatedPins = new ArrayList<>(this.pins);
              updatedPins.set(pinIndex, targetedPin.withState(Pin.State.HIGH));

              // Überprüfen, ob durch diesen Treffer alle Pins auf HIGH stehen -> Siegbedingung!
              Status nextStatus = updatedPins.stream().allMatch(pin -> pin.state().isSet()) ? Status.WON : Status.RUNNING;

              // (die Schlange darf dabei aber nicht auf den Pin gehen -> wir bewegen sie nicht und setzen Blickrichtung auf NONE)
              return new GameState(this.level, this.snake, updatedPins, nextStatus, Direction.NONE);
          }
      }
    // TODO: anderenfalls: bewege die Schlange um einen Schritt in Blickrichtung (falls gesetzt)
      List<Position> oldBody = this.snake.body();
      List<Position> newBody = new ArrayList<>(oldBody.size());

      newBody.add(nextHeadPos); // Neuer Kopf wird vorne eingefügt
      for (int i = 0; i < oldBody.size() - 1; i++) {
          newBody.add(oldBody.get(i)); // Die restlichen Glieder rücken nach
      }

      Snake movedSnake = new Snake(newBody);
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

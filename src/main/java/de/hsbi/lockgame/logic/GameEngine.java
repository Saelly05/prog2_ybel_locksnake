package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.Direction;
import de.hsbi.lockgame.model.Level;
import de.hsbi.lockgame.model.Snake;
import de.hsbi.lockgame.ui.GamePanel;
import java.util.ArrayList;
import java.util.List;

// TODO: Die GameEngine verwaltet den GameState.

// TODO: Die GameEngine wird durch den Timer im main() getriggert ("tick") und lässt den GameState
// daraufhin einen Schritt ausführen. Dann müssen alle für den GameState registrierten Observer
// benachrichtigt werden, damit das Spielfeld neu gezeichnet werden kann o.ä.

// TODO: Die GameEngine beobachtet die Tastatureingaben (gesetzt in GamePanel.setupKeyBindings()),
// die in Direction übersetzt und an GameEngine.update() übergeben werden. Wenn es eine neue Eingabe
// gibt, wird die "update"-Methode von GameEngine aufgerufen, und die GameEngine muss die
// Blickrichtung der Schlange aktualisieren und diese GameState-Änderung den für den GameState
// registrierten Observer mitteilen.

// TODO: Die GameEngine ist ein Observer für Direction: GameEngine.update(Direction)
// TODO: Die GameEngine ist ein Observable für GameState: GamePanel.update(GameState)
public final class GameEngine {
    private GameState gameState;
    private GamePanel gamePanel;

    private final List<GameStateObserver> observers = new ArrayList<>();

    @FunctionalInterface
    public interface GameStateObserver {
        void update(GameState state);
    }

  public GameEngine(Level level) {
    // TODO: lege eine neue GameEngine mit den übergebenen Informationen an
      this.gameState = new GameState(
          level,
          new Snake(List.of(level.snakeStart())),
          List.copyOf(level.pins()),
          GameState.Status.RUNNING,
          Direction.NONE
      );
  }

  public GameState state() {
    // TODO: gebe den aktuellen Spielzustand zurück
      return this.gameState;
  }

  public void setGamePanel(GamePanel panel) {
    // TODO: Setter
      this.gamePanel = panel;
  }
    public void addObserver(GameStateObserver observer) {
        this.observers.add(observer);
    }


    private void notifyObservers() {
        observers.forEach(observer -> observer.update(this.gameState));
    }

  public void update(Direction d) {
    // TODO: aktualisiere den Blickwinkel der Schlange (GameState)
    // TODO: benachrichtige alle Observer und gibt den neuen Spielzustand mit (Neuzeichnen der
    // Spielfläche)
      this.gameState = new GameState(
          this.gameState.level(),
          this.gameState.snake(),
          this.gameState.pins(),
          this.gameState.status(),
          d
      );
      notifyObservers(); // GUI sofort aktualisieren
  }
  }

  public void tick() {
    // TODO: lass das Spiel (den GameState) einen Schritt ("tick") machen
    // TODO: benachrichtige alle Observer und gibt den neuen Spielzustand mit (Neuzeichnen der
    // Spielfläche)
      this.gameState = this.gameState.tick();
      notifyObservers();
  }

  }


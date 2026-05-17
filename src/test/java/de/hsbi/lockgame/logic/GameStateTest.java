package de.hsbi.lockgame.logic;

import de.hsbi.lockgame.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;


class GameStateTest {

    private CellType[][] emptyCells;
    private Position defaultStart;

    /**
     * Erstellt vor jedem Testlauf eine reproduzierbare Test-Fixture (5x5 leeres Grid).
     */
    @BeforeEach
    void setUp() {
        emptyCells = new CellType[5][5];
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                emptyCells[x][y] = CellType.EMPTY;
            }
        }
        defaultStart = new Position(2, 2); // Startposition genau in der Mitte
    }

    /**
     * Hilfsmethode zur schnellen Erzeugung eines Levels mit variablen Pins.
     */
    private Level createLevel(List<Pin> pins) {
        return new Level(5, 5, emptyCells, pins, defaultStart);
    }

    /**
     * Hilfsmethode: Erzeugt einen frischen GameState mit den geforderten 5 Argumenten.
     */
    private GameState createInitialState(Level level) {
        return new GameState(
            level,
            new Snake(List.of(level.snakeStart())),
            level.pins(),
            GameState.Status.RUNNING,
            Direction.NONE
        );
    }

    // ==========================================
    // 1. INITIALZUSTAND & BASISBEWEGUNGEN
    // ==========================================

    @Test
    void testInitialStateIsRunning() {
        // Given: Ein frisch geladenes Level ohne Pins
        Level level = createLevel(List.of());

        // When: Der GameState initialisiert wird (mit 5 Argumenten)
        GameState state = createInitialState(level);

        // Then: Muss der Zustand RUNNING sein, die Schlange am Start stehen und keine Richtung haben
        assertEquals(GameState.Status.RUNNING, state.status(), "Das Spiel muss im Zustand RUNNING starten.");
        assertEquals(Direction.NONE, state.pendingDirection(), "Die anstehende Richtung muss initial NONE sein.");
        assertEquals(defaultStart, state.snake().head(), "Der Schlangenkopf muss auf der Startposition liegen.");
    }

    @Test
    void testTickWithoutDirectionDoesNotMove() {
        // Given: Ein Spiel im Initialzustand (pendingDirection ist NONE)
        GameState state = createInitialState(createLevel(List.of()));

        // When: Ein Spielschritt (tick) ausgefuehrt wird
        GameState nextState = state.tick();

        // Then: Darf sich die Schlange nicht bewegt haben und das Spiel bleibt unveraendert
        assertEquals(defaultStart, nextState.snake().head(), "Ohne Richtung darf sich die Schlange nicht bewegen.");
        assertEquals(GameState.Status.RUNNING, nextState.status());
    }

    @Test
    void testValidMovementForward() {
        // Given: Ein laufendes Spiel, bei dem sich die Schlange nach oben ausrichten soll
        GameState state = createInitialState(createLevel(List.of()));
        GameState stateWithDirection = new GameState(state.level(), state.snake(), state.pins(), state.status(), Direction.UP);

        // When: Die Engine die Logik weiterschaltet
        GameState nextState = stateWithDirection.tick();

        // Then: Muss der Schlangenkopf ein Feld nach oben gewandert sein (Y - 1)
        Position expectedPos = new Position(2, 1);
        // Wir vergleichen explizit die X- und Y-Werte statt der Objekte
        assertEquals(expectedPos.x(), nextState.snake().head().x(), "X-Koordinate falsch");
        assertEquals(expectedPos.y(), nextState.snake().head().y(), "Y-Koordinate falsch");
    }
    // ==========================================
    // 2. KOLLISIONEN & BLOCKADEN
    // ==========================================

    @Test
    void testOutOfBoundsCausesLoss() {
        // Given: Eine Schlange am aeussersten oberen Spielfeldrand (Y = 0), blickend nach UP
        Position edgeStart = new Position(2, 0);
        Level level = new Level(5, 5, emptyCells, List.of(), edgeStart);
        GameState state = createInitialState(level);
        GameState stateMovingUp = new GameState(state.level(), state.snake(), state.pins(), state.status(), Direction.UP);

        // When: Das Spiel weiterschaltet und die Grenzen verletzt werden
        GameState nextState = stateMovingUp.tick();

        // Then: Schlaegt die out-of-bounds Bedingung an und das Spiel ist verloren
        assertEquals(GameState.Status.LOST_OUT_OF_BOUNDS, nextState.status(), "Das Verlassen des Feldes muss zum Verlust fuehren.");
    }

    @Test
    void testWallCollisionBlocksSnake() {
        // Given: Eine Wand (WALL) direkt ueber der Startposition der Schlange
        emptyCells[2][1] = CellType.WALL;
        Level level = createLevel(List.of());
        GameState state = createInitialState(level);
        GameState stateMovingUp = new GameState(state.level(), state.snake(), state.pins(), state.status(), Direction.UP);

        // When: Die Schlange versucht, in die Wand zu kriechen
        GameState nextState = stateMovingUp.tick();

        // Then: Bleibt die Schlange vor der Wand stehen, die Richtung wird auf NONE zurueckgesetzt, Spiel laeuft weiter
        assertEquals(defaultStart, nextState.snake().head(), "Die Schlange darf sich nicht in die Wand bewegen.");
        assertEquals(Direction.NONE, nextState.pendingDirection(), "Bei Wandkontakt muss die pendingDirection auf NONE gehen.");
        assertEquals(GameState.Status.RUNNING, nextState.status());
    }

    @Test
    void testSelfCollisionCausesLoss() {
        // Given: Eine kuenstlich verlängerte Schlange, die im Kreis steht und sich selbst in die Flanke laeuft
        Level level = createLevel(List.of());
        List<Position> circularBody = List.of(
            new Position(2, 2), // Kopf
            new Position(2, 3), // Koerper
            new Position(3, 3), // Koerper
            new Position(3, 2)  // Schwanzglied rechts neben dem Kopf
        );
        Snake snake = new Snake(circularBody);
        // Schlange ist nach rechts (RIGHT) ausgerichtet, wo sich ihr eigenes Schwanzglied befindet
        GameState state = new GameState(level, snake, List.of(), GameState.Status.RUNNING, Direction.RIGHT);

        // When: Der Tick verarbeitet wird
        GameState nextState = state.tick();

        // Then: Muss die Selbstkollision erkannt werden und der Status auf LOST_SELF_COLLISION wechseln
        assertEquals(GameState.Status.LOST_SELF_COLLISION, nextState.status(), "Selbstkollision muss das Spiel beenden.");
    }

    // ==========================================
    // 3. PIN-INTERAKTIONEN & MECHANIK
    // ==========================================

    @Test
    void testPinCollisionWrongDirectionBlocks() {
        // Given: Ein ungesperrter Pin ueber der Schlange, der die Aktivierungsrichtung DOWN erwartet
        Position pinPos = new Position(2, 1);
        Pin wrongDirectionPin = new Pin(pinPos, Pin.State.LOW, Direction.DOWN);
        Level level = createLevel(List.of(wrongDirectionPin));

        // Schlange laeuft von unten nach oben (UP) an, trifft den Pin also verkehrt herum
        GameState state = createInitialState(level);
        GameState stateMovingUp = new GameState(state.level(), state.snake(), state.pins(), state.status(), Direction.UP);

        // When: Die Schlange mit der falschen Richtung auf den Pin trifft
        GameState nextState = stateMovingUp.tick();

        // Then: Wirkt der Pin wie eine Wand blockierend, bleibt LOW und die Bewegung stoppt
        assertEquals(Direction.NONE, nextState.pendingDirection(), "Falsche Anlaufrichtung muss die Bewegung blockieren.");
        assertEquals(Pin.State.LOW, nextState.pins().get(0).state(), "Der Pin darf nicht aktiviert werden.");
        assertEquals(defaultStart, nextState.snake().head(), "Die Schlange darf das Feld des Pins nicht betreten.");
    }

    @Test
    void testAlreadySetPinBlocks() {
        // Given: Ein Pin, der bereits auf HIGH (aktiviert) steht
        Position pinPos = new Position(2, 1);
        Pin activePin = new Pin(pinPos, Pin.State.HIGH, Direction.UP);
        Level level = createLevel(List.of(activePin));

        GameState state = createInitialState(level);
        GameState stateMovingUp = new GameState(state.level(), state.snake(), state.pins(), state.status(), Direction.UP);

        // When: Die Schlange versucht, den bereits gelockten Pin erneut anzulaufen
        GameState nextState = stateMovingUp.tick();

        // Then: Stoppt die Schlange exakt davor, verhaelt sich wie eine Wand
        assertEquals(Direction.NONE, nextState.pendingDirection(), "Ein bereits gesetzter Pin blockiert jegliche Weiterfahrt.");
        assertEquals(defaultStart, nextState.snake().head());
    }

    @Test
    void testPinActivationSuccess() {
        // Given: Ein korrekter Pin (Aktivierungsrichtung UP) und ein unbeteiligter zweiter Pin (damit das Spiel nicht sofort endet)
        Position pinPos = new Position(2, 1);
        Pin targetedPin = new Pin(pinPos, Pin.State.LOW, Direction.UP);
        Pin dummyPin = new Pin(new Position(4, 4), Pin.State.LOW, Direction.UP);
        Level level = createLevel(List.of(targetedPin, dummyPin));

        GameState state = createInitialState(level);
        GameState stateMovingUp = new GameState(state.level(), state.snake(), state.pins(), state.status(), Direction.UP);

        // When: Die Schlange in der exakt richtigen Richtung auf den Pin trifft
        GameState nextState = stateMovingUp.tick();

        // Then: Wird der Pin auf HIGH gesetzt, die Schlange stoppt davor (geht NICHT drauf) und das Spiel laeuft weiter
        assertTrue(nextState.pins().get(0).state().isSet(), "Der getroffene Pin muss nun den Zustand HIGH (isSet) besitzen.");
        assertFalse(nextState.pins().get(1).state().isSet(), "Der unbeteiligte Pin muss unveraendert LOW bleiben.");
        assertEquals(defaultStart, nextState.snake().head(), "Die Schlange muss laut Vorgabe VOR dem Pin anhalten.");
        assertEquals(Direction.NONE, nextState.pendingDirection());
        assertEquals(GameState.Status.RUNNING, nextState.status());
    }

    // ==========================================
    // 4. GEWINNBEDINGUNG
    // ==========================================

    @Test
    void testWinConditionAllPinsSet() {
        // Given: Nur ein einziger, letzter Pin im Level, der noch geknackt werden muss
        Position pinPos = new Position(2, 1);
        Pin lastPin = new Pin(pinPos, Pin.State.LOW, Direction.UP);
        Level level = createLevel(List.of(lastPin));

        GameState state = createInitialState(level);
        GameState stateMovingUp = new GameState(state.level(), state.snake(), state.pins(), state.status(), Direction.UP);

        // When: Die Schlange den letzten Pin erfolgreich ausloest
        GameState nextState = stateMovingUp.tick();

        // Then: Schaltet das Spiel sofort auf den Status WON um
        assertTrue(nextState.pins().get(0).state().isSet());
        assertEquals(GameState.Status.WON, nextState.status(), "Das Aktivieren des letzten offenen Pins muss zum Sieg (WON) fuehren.");
    }
}

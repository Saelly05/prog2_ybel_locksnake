classDiagram
direction TB

    %% Controller / App-Start
    class Main {
        +main()$
        -handleGameEnd(ActionEvent, GameState, JFrame)$
    }

    class GameEngine {
        -GameState gameState
        -GamePanel gamePanel
        -List~GameStateObserver~ observers
        +GameEngine(Level level)
        +state() GameState
        +setGamePanel(GamePanel panel)
        +addObserver(GameStateObserver observer)
        -notifyObservers()
        +update(Direction d)
        +tick()
    }

    class GameStateObserver {
        <<interface>>
        +update(GameState state)
    }

    %% UI / View
    class GamePanel {
        -GameState state
        -GameEngine gameEngine
        +GamePanel(GameState initialState, GameRenderer renderer)
        +update(GameState newState)
        +setGameEngine(GameEngine engine)
        -setupKeyBindings()
        #paintComponent(Graphics g)
    }

    %% Logic / State
    class GameState {
        -Level level
        -Snake snake
        -List~Pin~ pins
        -Status status
        -Direction pendingDirection
        +GameState(Level, Snake, List~Pin~, Status, Direction)
        +tick() GameState
    }

    %% Models
    class Level {
        -int width
        -int height
        -CellType[][] cells
        -List~Pin~ pins
        -Position snakeStart
        +isInside(Position pos) boolean
        +cellAt(Position pos) CellType
    }

    class Snake {
        -List~Position~ body
        +head() Position
        +nextHead(Direction d) Position
        +occupies(Position position) boolean
        +grow(Direction d) Snake
    }

    class Pin {
        -Position position
        -State state
        -Direction activationDirection
        +withState(State newState) Pin
    }

    class Position {
        -int x
        -int y
    }

    class LevelLoader {
        +loadLevelFromPath(Path)$ Level
        +loadLevelFromResource(String)$ Level
        -parseLines(List~String~)$ Level
    }

    %% Enums
    class Direction {
        <<enum>>
        UP
        DOWN
        LEFT
        RIGHT
        NONE
        +applyTo(Position pos) Position
        +oppositeDirection() Direction
    }

    class CellType {
        <<enum>>
        EMPTY
        WALL
        PIN_SLOT
    }

    %% Verbindungen / Beziehungen
    Main --> LevelLoader : nutzt
    Main --> GameEngine : erzeugt
    Main --> GamePanel : erzeugt

    GameEngine o-- GameState : verwaltet (Aggregation)
    GameEngine ..> GameStateObserver : benachrichtigt (Dependency)
    GamePanel ..|> GameStateObserver : implementiert
    GamePanel --> GameEngine : meldet Input an

    GameState *-- Level : besitzt (Komposition)
    GameState *-- Snake : besitzt
    GameState *-- Pin : besitzt
    GameState *-- Direction : nutzt

    Level *-- CellType : besteht aus
    Level *-- Pin : beinhaltet
    Snake *-- Position : besteht aus
    Pin *-- Position : platziert auf
    Pin *-- Direction : erfordert


Architekturkomponenten (MVC)

Model:
Die Datenhaltung liegt in den Paketen model und logic. Klassen wie Position, Snake, Pin und Level speichern den Spielzustand.
Das Modell ist immutable aufgebaut, daher werden bei Änderungen immer neue Objekte erstellt.

View:
Die Benutzeroberfläche befindet sich in ui und ui.render.
Das GamePanel zeigt das Spielfeld an und verarbeitet Tasteneingaben, während der Java2DRenderer das Zeichnen übernimmt.

Controller:
Die Steuerung erfolgt über die GameEngine und die Main-Klasse.
Die Engine verarbeitet Eingaben, berechnet neue Spielzustände und steuert den Spielfluss.

Verwendetes Design-Pattern

Es wurde das Observer-Pattern genutzt, um Oberfläche und Spiellogik zu trennen:

Die GameEngine informiert das GamePanel über Änderungen, damit die Anzeige aktualisiert wird.
Das GamePanel leitet Tasteneingaben an die GameEngine weiter.




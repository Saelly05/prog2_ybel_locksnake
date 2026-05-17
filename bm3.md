## Systemarchitektur (UML Klassendiagramm)

```mermaid
classDiagram
    direction TB
    class Main {
        +main(args)
    }
    class GameEngine {
        -GameState gameState
        -Level level
        +gameTick()
        +handleKeyPress(keyCode)
    }
    class GamePanel {
        -GameEngine gameEngine
        +paintComponent(g)
    }
    class GameState {
        -Snake snake
        -List~Pin~ pins
    }
    class Snake {
        -Position head
        +moveTo(nextPos)
    }
    class Pin {
        -Position position
        -boolean isLocked
    }

    Main --> GameEngine : erzeugt
    Main --> GamePanel : erzeugt
    GamePanel --> GameEngine : beobachtet (Observer)
    GameEngine *-- GameState : besitzt
    GameState *-- Snake : besitzt
    GameState *-- Pin : besitzt




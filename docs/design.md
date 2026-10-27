# Engine design after M5

These diagrams describe this repository's implementation at M5. `engine`
contains `Game` and the stateless `MoveGenerator`; `model` contains all the
remaining types below. There is currently no `BoardView` interface.

## Class diagram

All declared public methods are shown, including record accessors and canonical
constructors. Compiler-generated `equals`/`hashCode` and enum `values`/`valueOf`
are omitted. The six subclasses inherit the public `Piece` API; their common
`Color` constructors and move overrides are omitted as permitted by the handout.
`Pawn` additionally overrides `attacks`. Protected helpers are included to show
where the two shared movement algorithms live.

```mermaid
classDiagram
direction TB
class Game {
    -Board board
    -List~Move~ history
    -Color sideToMove
    +Game()
    +Game(Board board, Color sideToMove)
    +board() Board
    +sideToMove() Color
    +history() List~Move~
    +legalMoves() List~Move~
    +findLegalMove(String notation) Optional~Move~
    +play(Move move) void
    +undoLastMove() Optional~Move~
}
class MoveGenerator {
    <<utility>>
    -MoveGenerator()
    +legalMoves(Board board, Color color) List~Move~$
    +isInCheck(Board board, Color color) boolean$
    +isAttacked(Board board, Position target, Color attacker) boolean$
    ~pseudoLegalMoves(Board board, Color color) List~Move~$
}
class Board {
    -Piece[][] squares
    +Board()
    +pieceAt(Position position) Piece
    +isEmpty(Position position) boolean
    +place(Position position, Piece piece) void
    +apply(Move move) void
    +undo(Move move) void
    +positionsOf(Color color) List~Position~
    +kingPosition(Color color) Position
    +toString() String
}
class Piece {
    <<abstract>>
    -Color color
    -PieceType type
    #Piece(Color color, PieceType type)
    +color() Color
    +type() PieceType
    +symbol() char
    +toString() String
    +pseudoLegalMoves(Board board, Position from) List~Move~*
    +attacks(Board board, Position from, Position target) boolean
    #slidingMoves(Board board, Position from, int[][] directions) List~Move~
    #steppingMoves(Board board, Position from, int[][] offsets) List~Move~
}
class Knight
class Bishop
class Rook
class Queen
class King
class Pawn
class Move {
    <<record>>
    +Move(Position from, Position to, Piece moved, Piece captured, PieceType promotesTo)
    +from() Position
    +to() Position
    +moved() Piece
    +captured() Piece
    +promotesTo() PieceType
    +quiet(Position from, Position to, Piece moved) Move$
    +capture(Position from, Position to, Piece moved, Piece captured) Move$
    +promotion(Position from, Position to, Piece moved, Piece captured, PieceType promotesTo) Move$
    +isCapture() boolean
    +isPromotion() boolean
    +toString() String
}
class Position {
    <<record>>
    +int BOARD_SIZE$
    +Position(int file, int rank)
    +file() int
    +rank() int
    +isOnBoard(int file, int rank) boolean$
    +parse(String algebraic) Position$
    +offsetOrNull(int fileDelta, int rankDelta) Position
    +toString() String
}
class Color {
    <<enumeration>>
    WHITE
    BLACK
    +opposite() Color
    +pawnDirection() int
    +pawnStartRank() int
    +promotionRank() int
}
class PieceType {
    <<enumeration>>
    PAWN
    KNIGHT
    BISHOP
    ROOK
    QUEEN
    KING
    +symbol() char
    +fromSymbol(char letter) PieceType$
}
Piece <|-- Knight
Piece <|-- Bishop
Piece <|-- Rook
Piece <|-- Queen
Piece <|-- King
Piece <|-- Pawn
Game "1" o-- "1" Board : holds supplied or standard board
Game "1" *-- "0..*" Move : owns history list entries
Game --> Color : sideToMove
Game ..> MoveGenerator : delegates legalMoves
Game ..> BoardFactory : default constructor
class BoardFactory {
    <<external factory package>>
}
Board "1" o-- "0..64" Piece : private squares
Board ..> Position : square queries
Board ..> Move : applies and undoes
Board ..> PieceType : king lookup and promotion
Board ..> Queen : creates promotion
Board ..> Rook : creates promotion
Board ..> Bishop : creates promotion
Board ..> Knight : creates promotion
Piece --> Color : fixed color
Piece --> PieceType : fixed type
Piece ..> Board : reads occupants
Piece ..> Position : offsets
Piece ..> Move : generates
Move --> "2" Position : from and to
Move --> "1" Piece : moved
Move --> "0..1" Piece : captured
Move --> "0..1" PieceType : promotesTo
MoveGenerator ..> Board : queries and trial mutations
MoveGenerator ..> Color : side and opponent
MoveGenerator ..> Piece : moves and attacks
MoveGenerator ..> Position : occupied and king squares
MoveGenerator ..> Move : candidates and results
```

The hollow diamonds denote stored references: a supplied `Board` may also be
held by its caller, and pieces may be referenced by both the board and moves.
The filled diamond denotes `Game`'s internally created history collection;
`Move` values in it may also be held by callers. `history()` returns an immutable
snapshot. `MoveGenerator` stores no references between calls. An empty square,
a missing king, and the optional capture/promotion fields use `null`.

## Sequence: `game.legalMoves()`

The `Piece` lifeline represents each piece encountered. Helpers called within
`Piece` use `Board.pieceAt` and `Position.offsetOrNull`; those inner movement
loops are summarized by the `pseudoLegalMoves` response. `kingPosition` scans
the board's occupied squares of the requested color. Opponent attack checks
stop at the first attacker. The candidate board is always undone in `finally`.

```mermaid
sequenceDiagram
    actor Caller
    participant Game
    participant Generator as MoveGenerator
    participant Board
    participant Piece

    Caller->>Game: legalMoves()
    Game->>Generator: legalMoves(board, sideToMove)
    Generator->>Generator: pseudoLegalMoves(board, color)
    Generator->>Board: positionsOf(color)
    Board-->>Generator: occupied squares
    loop each friendly square from
        Generator->>Board: pieceAt(from)
        Board-->>Generator: piece
        Generator->>Piece: pseudoLegalMoves(board, from)
        Piece-->>Generator: geometric moves, captures, promotions
        Note over Generator: Append moves to candidates
    end
    loop each candidate move
        Generator->>Board: apply(move)
        Note over Board: Clear source, place mover or promoted piece at destination
        Generator->>Generator: isInCheck(board, color)
        Generator->>Board: kingPosition(color)
        Board-->>Generator: king square or null
        alt king is absent
            Note over Generator: inCheck = false
        else king is present
            Generator->>Generator: isAttacked(board, king, color.opposite())
            Generator->>Board: positionsOf(attacker)
            Board-->>Generator: enemy squares
            loop enemy squares until an attack is found
                Generator->>Board: pieceAt(from)
                Board-->>Generator: enemy piece
                Generator->>Piece: attacks(board, from, king)
                Piece-->>Generator: true or false
            end
            Note over Generator: inCheck = whether any enemy attacks
        end
        alt king is safe
            Note over Generator: Keep candidate in legal list
        else king remains attacked
            Note over Generator: Drop candidate
        end
        Generator->>Board: undo(move) in finally
        Note over Board: Restore original mover and captured occupant
    end
    Generator-->>Game: legal moves, board restored
    Game-->>Caller: legal moves
```

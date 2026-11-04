# M6 submission

## 1. Who decides, who shows

`MoveGenerator.status(Board, Color)` detects checkmate and stalemate by combining
`isInCheck` with `legalMoves`; `Game.status()` delegates to it. This answers the
session's rule question: whether a position ends the game is independent of its
display. `GameController.announceResult()` coordinates the response by sending
the result to `BoardView.showMessage`, while `ConsoleView` or `EmojiView` performs
the actual display; those answer the request-coordination and presentation
questions respectively.

## 2. The dependency rule

Outside the `view` package, only `Main` imports
`edu.sfsu.csc413.chess.view`: it constructs the chosen `BoardView` and connects it
to `Game` through `GameController`. Inside `view`, the classes refer to their own
package's types directly, without needing imports for them. Classes in `engine`,
`model`, and `factory` must not import `view`, because rules, storage, and object
creation must continue to work with any presentation, including the tests'
recording view.

## 3. Test 2 on this engine

For M7 I expect to add a `SwingView` implementing `BoardView` in a new source file
and update `Main.java` to select it; extra Swing UI helper files can stay in the
view package if needed. `SwingView.start(GameController)` can register event
handlers that pass `PlayerAction` objects to `handle`, and override
`promotionChoice(Game)` to offer a dialog. `GameController` and all files in
`engine` should remain unchanged, since event-driven views do not need to call
`runLoop()` or implement `nextAction`.

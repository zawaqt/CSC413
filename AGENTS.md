# Instructions for coding agents — M7

This is the CSC 413 chess engine built with MVC. The existing M6 engine and
controller are the fixed baseline. M7 is views only.

## Do

- Add views as new classes in `src/main/java/edu/sfsu/csc413/chess/view/`
  or a package beneath it. Read `BoardView`, `ConsoleView`, `PlayerAction`,
  and `GameController` before writing a view.
- Read the game through `Game.board()`, `sideToMove()`, `legalMoves()`,
  `status()`, and `history()`. Use the returned board only for queries.
- Send requests through `controller.handle(new PlayerAction.Move(notation))`
  and the other `PlayerAction` records. The controller alone changes the game.
- Update the existing view-selection expression in `Main.main` to select
  `swing` and the additional view; this repository has no `Main.chooseView`.
- Keep game queries, click handling, and drawing on Swing's event-dispatch
  thread. `render` is called before `start`; retain the game/messages until
  the window exists. Give every square an accessible algebraic name.
- Pieces must be readable on light and dark squares; prefer explicit
  contrasting text and backgrounds over relying on a font's glyph outline.

## Do not

- Do not change `model/`, `engine/`, `factory/`, `pom.xml`, or existing tests.
- Do not change `BoardView`, `PlayerAction`, `GameController`, `ConsoleView`,
  `EmojiView`, `PieceGlyphs`, or `TextBoardRenderer`.
- Do not call `game.play`, `game.undoLastMove`, `Board.place`, `Board.apply`,
  or `Board.undo` from a view, and do not derive movement rules there.
- Do not wait for the next move inside a window: `start` builds its window
  and returns; a Swing view never calls `controller.runLoop()`.
  A modal dialog asking one question, such as a promotion choice, is fine.
- Do not claim tests, debugger observations, human review, or manual edits
  occurred unless they actually did. The `M7 by hand:` behavior change must
  be made by the user without agent assistance; leave it pending.

## Independent generation runs

- The solo M7 comparison requires three fresh agent sessions. The coordinator
  must delegate each generation run to a fresh subagent, with no conversation
  history, in its own worktree/branch based on the same `main` commit.
- Read this file in each run. Use the same model and change exactly one
  instruction between consecutive run requests. Do not read other runs.
- Each run generates both a Swing view and a playable JDK-only bordered
  terminal view, selectable as `swing` and `border`.
- Commit each run, report what actually passed and what remains untested,
  and leave merging, comparison, GUI checks, and submission to the coordinator.

## Checks and scope

- Run `mvnw.cmd test`: all 83 baseline tests must pass unchanged.
- Check the diff against `submit-m6`: allowed changes are new view files,
  new view tests, `Main.java`, this file, optional `CLAUDE.md`,
  `m7-submission.md`, and the incoming `docs/milestones/m7.md`.
- Search new views for direct game mutation and the Swing view for `runLoop`.
- The coordinator must test each run with Fool's Mate, an illegal move,
  and Undo, compare selection, legal-move lookup and promotion, then apply
  an idea from a run that was not kept.
- If a prohibited file appears necessary, explain the conflict and leave
  that file unchanged. Do not fabricate the required human-only work or
  tag the final M7 submission while it remains outstanding.

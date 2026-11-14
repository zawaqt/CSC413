# M7 — Views, generated and compared

**Status: implementation and agent-assisted checks complete; student-only
behavior change pending.** This document records actual agent work and
observations. It does not claim that the student performed the reviews,
debugger session, or a hand-written modification. `submit-m7` is intentionally
not created until the required human-only step is complete.

## 1. Group

Alone is the working assumption for this repository; no groupmates' code or
identities were supplied or used. The owner should confirm this before final
submission, or replace it with their actual group membership.

## 2. Tools and prior-work disclosure

OpenAI Codex, GPT-6, generated the views. Three fresh subagent sessions used
the same inherited model, no conversation history, and separate worktrees.
The coordinating Codex session reviewed, tested, merged, and documented them.
The tests use Maven/JUnit and only JDK Swing; desktop inspection used Computer
Use, and breakpoint evidence used the JDK Java Debug Interface (JDI).

There was no `M6 fix:` commit during M7. `GameController` and the M6 rules were
left byte-for-byte unchanged. However, earlier work in this repository,
including the M6 controller, was implemented with Codex assistance before this
M7 handout was read. That earlier work must not be represented as student-only
hand-written code; leaving it unchanged now does not retroactively satisfy the
handout's requirement about its authorship.

## 3. Other view

The command-line word is **`border`**:

```sh
java -cp target/classes edu.sfsu.csc413.chess.Main border
java -cp target/classes edu.sfsu.csc413.chess.Main swing
```

`BorderView` draws ASCII cell borders and file/rank coordinates. It rotates
both axes to face the side to move, parses existing actions with
`PlayerAction.parse`, treats EOF as Quit, and can play through checkmate.
This changes layout and perspective, rather than only substituting glyphs.

## 4. Generation runs and exact requests

Instructions were committed first as `1f77c99` (after merging `m7`). Every run
started from that same main commit in its own worktree, read `AGENTS.md`, and
was explicitly prohibited from inspecting other runs. The dispatch context
identified only its worktree/branch and required committing there, reporting
actual checks, and leaving desktop interaction to the coordinator.

### Run 1

- Branch: `m7-run-1`
- Generated commit: `f9d71beff19d0f966d3971050403fda8604e0a5c`
- Worktree: `target/m7-run-1`
- Request, word for word:

> Follow AGENTS.md. Generate a JDK-only Swing BoardView with an 8x8 click-source-then-destination board, high-contrast piece letters, turn/status and message labels, Undo and Quit buttons, accessible algebraic square names, and default queen promotion. Also generate a bordered terminal BoardView drawn from the side to move, supporting the existing PlayerAction commands. Select these with Main arguments swing and border while preserving console and emoji. Run the unchanged 83 tests, check the allowed diff and forbidden calls, and commit your run. Do not claim GUI or debugger checks that you did not perform.

### Run 2

- Branch: `m7-run-2`
- Generated commit: `7e7a82f527cc6ab19322857cf2da8a7f319d5f76`
- Worktree: `target/m7-run-2`
- The only request change from run 1 is the final instruction about highlights.
- Request, word for word:

> Follow AGENTS.md. Generate a JDK-only Swing BoardView with an 8x8 click-source-then-destination board, high-contrast piece letters, turn/status and message labels, Undo and Quit buttons, accessible algebraic square names, and default queen promotion. Also generate a bordered terminal BoardView drawn from the side to move, supporting the existing PlayerAction commands. Select these with Main arguments swing and border while preserving console and emoji. Run the unchanged 83 tests, check the allowed diff and forbidden calls, and commit your run. Do not claim GUI or debugger checks that you did not perform. Highlight the selected square and all its legal destinations by querying game.legalMoves().

### Run 3

- Branch: `m7-run-3`
- Generated commit: `5c52bfde7f99b5f7f15d5ca79778f8bd8b83f917`
- Worktree: `target/m7-run-3`
- The only request change from run 2 is the final instruction about coordinates.
- Request, word for word:

> Follow AGENTS.md. Generate a JDK-only Swing BoardView with an 8x8 click-source-then-destination board, high-contrast piece letters, turn/status and message labels, Undo and Quit buttons, accessible algebraic square names, and default queen promotion. Also generate a bordered terminal BoardView drawn from the side to move, supporting the existing PlayerAction commands. Select these with Main arguments swing and border while preserving console and emoji. Run the unchanged 83 tests, check the allowed diff and forbidden calls, and commit your run. Do not claim GUI or debugger checks that you did not perform. Highlight the selected square and all its legal destinations by querying game.legalMoves(). Add visible file and rank coordinates around the Swing board.

The prompts changed one instruction at a time, not the model or the baseline.
This does not prove a feature was caused by that instruction: run 1 generated
highlights without being explicitly asked, and run 2 added coordinates inside
its squares. Those overlaps are actual outcomes, not controlled differences.

## 5. AGENTS.md adaptation

`AGENTS.md` adapts session 17's selection rule to `Main.main`: this repository
has no `chooseView` method. It includes the corrected modal-dialog rule from
session 17 section 5. It also adds an EDT/startup rule: initial messages and
`render` arrive before `start`, so retain them and marshal game queries,
clicks, and rendering onto Swing's event-dispatch thread. This guards against
the session 18 concurrency failure without editing the model/controller.

The same file was shared by all three fresh sessions. It also directs the
coordinator to retain truthful evidence and leave the student's human-only
behavior change pending.

## 6. Agent-assisted review of retained code

These are the coordinator's actual checks and decisions; the student should
read and independently verify them before claiming personal review.

| Place | What it does | Check and decision |
|---|---|---|
| `view/SwingView.java:37`, `:46`, `:66` | `onEdt`, retained render state, and asynchronous startup keep game access and drawing on one thread. | Kept. Reviewed scheduling and started the real window; initial board/messages appeared. The debugger reported `AWT-EventQueue-0` for both clicks and controller dispatch. |
| `view/SwingView.java:129`, `:148` | `click` keeps a selected square, constructs notation, and `request` forwards a `PlayerAction` to the controller. | Kept. Real e2/e4 clicks generated `notation=e2e4`; Undo restored the starting position. An e2/e5 attempt displayed the controller's rejection without moving the pawn. |
| `view/SwingView.java:159`, `:163` | `draw` obtains legal destinations from `game.legalMoves()`. | Kept. Selecting e2 visibly outlined e3 and e4. No movement geometry is reconstructed, and no direct mutation calls occur in the view. |
| `view/SwingView.java:112` | The added Legal moves button sends `PlayerAction.ShowMoves`. | Changed after comparison. Reimplemented the idea from run 1; the committed Swing integration test checks that it displays `20 legal moves:` at the opening. |
| `view/SwingView.java:193` | `close` leaves a terminal board visible, while an explicit Quit disposes the window. | Kept. The actual window displayed `WHITE to move | CHECKMATE` and `Checkmate. BLACK wins.` after Fool's Mate; Quit closed it. Tests cover the final display and disposal. |
| `view/BorderView.java:25`, `:31`, `:34` | `render` rotates rank and file indexing together when Black is on turn. | Kept. All three run checks reached checkmate, with both file legends observed; the committed border test checks both orientations and game history. |

Paths above are relative to `src/main/java/edu/sfsu/csc413/chess/`.

## 7. Comparison

The file in each comparison row is
`src/main/java/edu/sfsu/csc413/chess/view/SwingView.java` on that run's branch.

| Dimension | Run 1 | Run 2 | Run 3 |
|---|---|---|---|
| Half-finished move | `:34` stores `source`, with a cached `selectedMoves` list at `:35`; `render` clears both. | `:33` stores only `selected`; `render` clears it. | `:34` stores only `selected`; `render` clears it. |
| Legal moves | `:145` filters `game.legalMoves()` when selecting, then draws its cache. | `:160` queries `game.legalMoves()` during drawing and builds a destination set. | `:159` queries `game.legalMoves()` during drawing and builds a destination set. |
| Promotion | `click` at `:136` dispatches four-character notation; inherited `BoardView.promotionChoice` returns queen. | `click` at `:117` does the same; no view-specific promotion rule. | `click` at `:125` does the same; no view-specific promotion rule. |
| Layout | `start` at `:69` makes an 8x8 grid with `W`/`B` prefixes and a Legal moves button at `:109`. | `draw` at `:156` embeds each algebraic square below its piece in HTML. | `start` at `:65` makes a 10x10 layout whose outer cells label the 8x8 board. |
| Completion | `close` at `:193` disposes immediately, including at checkmate. | `close` at `:187` keeps the terminal position visible for Undo; explicit Quit disposes. | `close` at `:188` keeps the terminal position visible for Undo; explicit Quit disposes. |

The coordinator kept **run 3**. Its outer coordinates leave each playing
square with a single large letter, and its destination set can be regenerated
without coordinating a second selection cache. Run 1's prefixed pieces are
explicit, but its immediate disposal hides the last position; run 2's HTML
labels give square names everywhere but add markup to every draw. All three
passed the gameplay checks, so these are maintainability/presentation choices,
not claims that the rejected runs were broken.

## 8. Applied idea

Run 1's `SwingView.java:109` offered a Legal moves button that run 3 lacked.
After merging run 3, the coordinator implemented that idea directly with a
`JButton` and a listener dispatching `new PlayerAction.ShowMoves()` in the
retained `SwingView.java:112`. Commit `209c8bb` also adds a message tooltip so
the long sorted move list remains accessible. This is an agent-authored
application of the idea from a rejected run, not the student's by-hand change.

## 9. Debugger and verification evidence

No reproducible chess/click bug was found in the generated runs during these
checks. A temporary JDK JDI client therefore attached to the retained Swing
process on localhost, set real breakpoints at `SwingView.click` and
`GameController.handle`, and resumed after recording each stop. It did not
modify either class. Before clicking, the prediction was: e2 maps to `(4,1)`,
e4 maps to `(4,3)`, and the second click dispatches `Move("e2e4")`.

The observed stops were:

```text
STOP SwingView:129 thread=AWT-EventQueue-0
ARG Position[file=4, rank=1]
FIELD selected null
STOP SwingView:129 thread=AWT-EventQueue-0
ARG Position[file=4, rank=3]
FIELD selected Position[file=4, rank=1]
STOP GameController:74 thread=AWT-EventQueue-0
ARG PlayerAction$Move[notation=e2e4]
```

This was the coding agent's debugger session, not a student-operated one.
Native desktop clicks then verified Undo, e2e5 rejection, and the complete
`f2f3 e7e5 g2g4 d8h4` sequence. The window showed both terminal status and
the correct winner; its captured stderr contained no exception trace.

For **each run**, the unchanged 83 tests passed; the allowed diff and forbidden
calls were checked. A temporary JDK integration harness instantiated each
run's actual Swing components on the EDT and exercised their button listeners
for rejection, a valid move, Undo, Fool's Mate, winner display, and promotion.
It also ran each bordered view through a complete game with illegal input and
Undo. These were component-level checks, not separate native mouse sessions
for every rejected run.

The retained implementation has five additional committed tests:
`BorderViewTest` (three) and `SwingViewTest` (two). The latter use Swing on the
EDT and skip only when there is no display; all five actually ran here.
Final local result: **88 tests, 0 failures, 0 errors, 0 skipped**. The original
83 tests are unchanged. No model, engine, factory, controller, existing view,
or `pom.xml` changes are part of M7.

## 10. By hand — pending student action

No `M7 by hand:` commit has been made. The handout requires the student to
change one Swing behavior personally, without an agent, and commit that work
with that prefix. The student must supply the actual commit and describe what
they read to make their chosen change; neither the code change nor that
explanation will be fabricated by the agent.

After that step, confirm the group statement, verify this disclosure against
the code and run branches, replace this pending section with the true commit
and explanation, rerun the tests, and create/push `submit-m7` at the complete
submission. The implementation, comparison branches, and current evidence can
be pushed now without claiming final completion.

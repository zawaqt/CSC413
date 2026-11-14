package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class BorderViewTest {
    @Test
    void scriptCanRejectUndoAndFinishAGame() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Game game = new Game();
        BorderView view = new BorderView(new PrintStream(output), new Scanner(
                "e2e5\ne2e4\nundo\nf2f3\ne7e5\ng2g4\nd8h4\n"));
        new GameController(game, view).start();

        assertEquals(GameStatus.CHECKMATE, game.status());
        assertEquals(4, game.history().size());
        assertTrue(output.toString().contains("Not a legal move: 'e2e5'."));
        assertTrue(output.toString().contains("Checkmate. BLACK wins."));
        assertTrue(output.toString().contains("a   b   c   d   e   f   g   h"));
        assertTrue(output.toString().contains("h   g   f   e   d   c   b   a"));
    }

    @Test
    void endOfInputQuitsWithoutPlaying() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Game game = new Game();
        new GameController(game, new BorderView(new PrintStream(output), new Scanner(""))).start();
        assertTrue(game.history().isEmpty());
        assertTrue(output.toString().contains("Goodbye."));
    }

    @Test
    void renderDoesNotChangeTheGame() {
        Game game = new Game();
        String before = game.board().toString();
        new BorderView(new PrintStream(new ByteArrayOutputStream()), new Scanner("")).render(game);
        assertEquals(before, game.board().toString());
        assertTrue(game.history().isEmpty());
    }
}

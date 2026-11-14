package edu.sfsu.csc413.chess;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.view.BoardView;
import edu.sfsu.csc413.chess.view.ConsoleView;
import edu.sfsu.csc413.chess.view.EmojiView;
import edu.sfsu.csc413.chess.view.GameController;
import edu.sfsu.csc413.chess.view.SwingView;
import edu.sfsu.csc413.chess.view.BorderView;

/**
 * Creates the game, selects a console view, and starts their controller.
 */
public final class Main {

    public static void main(String[] args) {
        Game game = new Game();
        BoardView view = switch (args.length > 0 ? args[0].toLowerCase(java.util.Locale.ROOT) : "console") {
            case "swing" -> new SwingView();
            case "border" -> new BorderView();
            case "emoji" -> new EmojiView();
            default -> new ConsoleView();
        };
        new GameController(game, view).start();
    }

    private Main() {
    }
}

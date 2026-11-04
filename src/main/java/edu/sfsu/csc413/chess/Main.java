package edu.sfsu.csc413.chess;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.view.BoardView;
import edu.sfsu.csc413.chess.view.ConsoleView;
import edu.sfsu.csc413.chess.view.EmojiView;
import edu.sfsu.csc413.chess.view.GameController;

/**
 * Creates the game, selects a console view, and starts their controller.
 */
public final class Main {

    public static void main(String[] args) {
        Game game = new Game();
        BoardView view = args.length > 0 && "emoji".equalsIgnoreCase(args[0])
                ? new EmojiView() : new ConsoleView();
        new GameController(game, view).start();
    }

    private Main() {
    }
}

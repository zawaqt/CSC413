package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Position;

import java.io.PrintStream;
import java.util.Scanner;

/** ASCII cell borders and coordinates, rotated to face the player on turn. */
public final class BorderView implements BoardView {
    private final PrintStream out;
    private final Scanner in;

    public BorderView() {
        this(System.out, new Scanner(System.in));
    }

    public BorderView(PrintStream out, Scanner in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void render(Game game) {
        boolean flipped = game.sideToMove() == Color.BLACK;
        String border = "  +---+---+---+---+---+---+---+---+";
        out.println();
        out.println(border);
        for (int row = 0; row < 8; row++) {
            int rank = flipped ? row : 7 - row;
            out.print((rank + 1) + " |");
            for (int column = 0; column < 8; column++) {
                int file = flipped ? 7 - column : column;
                out.print(" " + PieceGlyphs.LETTERS.glyphFor(
                        game.board().pieceAt(new Position(file, rank))) + " |");
            }
            out.println(" " + (rank + 1));
            out.println(border);
        }
        out.print("   ");
        for (int column = 0; column < 8; column++) {
            out.print(" " + (char) ('a' + (flipped ? 7 - column : column)) + "  ");
        }
        out.println();
        out.println(game.sideToMove() + " to move | " + game.status());
        out.println("Uppercase: White; lowercase: Black.");
    }

    @Override
    public void showMessage(String message) {
        out.println(message);
    }

    @Override
    public void start(GameController controller) {
        controller.runLoop();
    }

    @Override
    public PlayerAction nextAction(Game game) {
        out.print("Move (e2e4, e7e8q), undo, moves, quit: ");
        out.flush();
        return PlayerAction.parse(in.hasNextLine() ? in.nextLine() : "quit");
    }
}

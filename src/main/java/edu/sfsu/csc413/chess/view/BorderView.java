package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import java.io.PrintStream;
import java.util.Scanner;

/** ASCII bordered board, facing the player whose turn it is. */
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
        boolean white = game.sideToMove() == Color.WHITE;
        String edge = "  +---+---+---+---+---+---+---+---+";
        out.println();
        out.println(edge);
        for (int row = 0; row < 8; row++) {
            int rank = white ? 7 - row : row;
            out.print((rank + 1) + " | ");
            for (int col = 0; col < 8; col++) {
                int file = white ? col : 7 - col;
                Piece piece = game.board().pieceAt(new Position(file, rank));
                out.print((piece == null ? "." : piece.symbol()) + " | ");
            }
            out.println(rank + 1);
            out.println(edge);
        }
        out.print("    ");
        for (int col = 0; col < 8; col++) {
            out.print((char) ('a' + (white ? col : 7 - col)) + "   ");
        }
        out.println();
        out.println(game.sideToMove() + " to move | " + game.status());
        out.println("White: uppercase; Black: lowercase. Promotion: append q/r/b/n (default q).");
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
        out.print("Move (e2e4), undo, moves, quit: ");
        out.flush();
        return PlayerAction.parse(in.hasNextLine() ? in.nextLine() : "quit");
    }
}

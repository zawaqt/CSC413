package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

/** An immutable chess piece with a color and type. */
public abstract class Piece {
    private final Color color;
    private final PieceType type;

    protected Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public Color color() {
        return color;
    }

    public PieceType type() {
        return type;
    }

    public abstract List<Move> pseudoLegalMoves(Board board, Position from);

    /** Default attack geometry follows this piece's possible moves. */
    public boolean attacks(Board board, Position from, Position target) {
        return pseudoLegalMoves(board, from).stream()
                .anyMatch(move -> move.to().equals(target));
    }

    /** Follow each ray until the board edge or the first occupant. */
    protected List<Move> slidingMoves(Board board, Position from, int[][] directions) {
        List<Move> moves = new ArrayList<>();
        for (int[] direction : directions) {
            Position to = from.offsetOrNull(direction[0], direction[1]);
            while (to != null) {
                Piece occupant = board.pieceAt(to);
                if (occupant == null) {
                    moves.add(Move.quiet(from, to, this));
                } else {
                    if (occupant.color() != color) {
                        moves.add(Move.capture(from, to, this, occupant));
                    }
                    break;
                }
                to = to.offsetOrNull(direction[0], direction[1]);
            }
        }
        return moves;
    }

    /** Try each offset once, skipping friendly occupants and off-board squares. */
    protected List<Move> steppingMoves(Board board, Position from, int[][] offsets) {
        List<Move> moves = new ArrayList<>();
        for (int[] offset : offsets) {
            Position to = from.offsetOrNull(offset[0], offset[1]);
            if (to == null) {
                continue;
            }
            Piece occupant = board.pieceAt(to);
            if (occupant == null) {
                moves.add(Move.quiet(from, to, this));
            } else if (occupant.color() != color) {
                moves.add(Move.capture(from, to, this, occupant));
            }
        }
        return moves;
    }

    /** This piece's FEN letter: uppercase for white and lowercase for black. */
    public char symbol() {
        char letter = type.symbol();
        return color == Color.WHITE ? letter : Character.toLowerCase(letter);
    }

    @Override
    public String toString() {
        return String.valueOf(symbol());
    }
}

package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

/** Stores which piece, if any, occupies each square of a chess board. */
public class Board {
    /** Indexed by [file][rank], both 0-based, matching {@link Position}. */
    private final Piece[][] squares;

    /** Create an empty board. */
    public Board() {
        squares = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];
    }

    /** Return the piece at {@code position}, or {@code null} when it is empty. */
    public Piece pieceAt(Position position) {
        return squares[position.file()][position.rank()];
    }

    /** Return whether {@code position} contains no piece. */
    public boolean isEmpty(Position position) {
        return pieceAt(position) == null;
    }

    /** Place a piece, replacing any occupant; {@code null} clears the square. */
    public void place(Position position, Piece piece) {
        squares[position.file()][position.rank()] = piece;
    }

    /** Apply a trusted move without checking its legality. */
    public void apply(Move move) {
        Piece destination = move.isPromotion()
                ? createPromoted(move.promotesTo(), move.moved().color())
                : move.moved();
        place(move.from(), null);
        place(move.to(), destination);
    }

    /** Restore both the original mover and the captured piece, if any. */
    public void undo(Move move) {
        place(move.from(), move.moved());
        place(move.to(), move.captured());
    }

    /* Keep model independent of factory. This duplicates the four promotion
       constructors in PieceFactory, so changes must be kept in sync there. */
    private Piece createPromoted(PieceType type, Color color) {
        return switch (type) {
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            default -> throw new IllegalArgumentException("Invalid promotion type: " + type);
        };
    }

    /** Return every position occupied by a piece of {@code color}. */
    public List<Position> positionsOf(Color color) {
        List<Position> positions = new ArrayList<>();
        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
                Piece piece = squares[file][rank];
                if (piece != null && piece.color() == color) {
                    positions.add(new Position(file, rank));
                }
            }
        }
        return positions;
    }

    /** Return the board's FEN placement field, with rank 8 first. */
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (int rank = Position.BOARD_SIZE - 1; rank >= 0; rank--) {
            int emptyRun = 0;
            for (int file = 0; file < Position.BOARD_SIZE; file++) {
                Piece piece = squares[file][rank];
                if (piece == null) {
                    emptyRun++;
                } else {
                    if (emptyRun > 0) {
                        text.append(emptyRun);
                        emptyRun = 0;
                    }
                    text.append(piece.symbol());
                }
            }
            if (emptyRun > 0) {
                text.append(emptyRun);
            }
            if (rank > 0) {
                text.append('/');
            }
        }
        return text.toString();
    }
}

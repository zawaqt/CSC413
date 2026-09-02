package edu.sfsu.csc413.chess.model;

/** A square on the chess board, represented by 0-based file and rank. */
public record Position(int file, int rank) {

    /** Files and ranks both run 0..7. */
    public static final int BOARD_SIZE = 8;

    /** True when these raw coordinates name a real square. */
    public static boolean isOnBoard(int file, int rank) {
        return file >= 0 && file < BOARD_SIZE && rank >= 0 && rank < BOARD_SIZE;
    }

    /** Reject coordinates that do not name a square on the board. */
    public Position {
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException(
                    "Position off board: file=" + file + ", rank=" + rank);
        }
    }

    /** Parse a two-character algebraic square such as {@code e2}. */
    public static Position parse(String algebraic) {
        if (algebraic == null || algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid position: " + algebraic);
        }

        int file = Character.toLowerCase(algebraic.charAt(0)) - 'a';
        int rank = algebraic.charAt(1) - '1';
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException("Invalid position: " + algebraic);
        }
        return new Position(file, rank);
    }

    /** Return the offset square, or {@code null} when it would be off-board. */
    public Position offsetOrNull(int fileDelta, int rankDelta) {
        int offsetFile = file + fileDelta;
        int offsetRank = rank + rankDelta;
        return isOnBoard(offsetFile, offsetRank)
                ? new Position(offsetFile, offsetRank)
                : null;
    }

    /** Return standard algebraic notation for this square. */
    @Override
    public String toString() {
        return "" + (char) ('a' + file) + (char) ('1' + rank);
    }
}

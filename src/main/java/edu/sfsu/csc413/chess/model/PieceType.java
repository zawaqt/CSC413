package edu.sfsu.csc413.chess.model;

/** The six kinds of chess piece and their standard FEN symbols. */
public enum PieceType {
    PAWN('P'),
    KNIGHT('N'),
    BISHOP('B'),
    ROOK('R'),
    QUEEN('Q'),
    KING('K');

    private final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    /** The uppercase letter for this type, as used in FEN notation. */
    public char symbol() {
        return symbol;
    }

    /** Return the piece type represented by {@code letter}, in either case. */
    public static PieceType fromSymbol(char letter) {
        char uppercase = Character.toUpperCase(letter);
        for (PieceType type : values()) {
            if (type.symbol == uppercase) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown piece symbol: " + letter);
    }
}

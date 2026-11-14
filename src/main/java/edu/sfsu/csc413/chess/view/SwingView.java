package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/** Event-driven board. Only the controller handles moves and undo requests. */
public final class SwingView implements BoardView {
    private final JButton[][] squares = new JButton[8][8];
    // All fields below, including the retained pre-start state, belong to the EDT.
    private Game game;
    private GameController controller;
    private JFrame frame;
    private JLabel status;
    private JLabel message;
    private JLabel selection;
    private String pendingMessage = "Choose a piece, then a destination.";
    private Position source;
    private List<Move> selectedMoves = List.of();
    private boolean closed;

    private static void onEdt(Runnable action) {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
        } else {
            SwingUtilities.invokeLater(action);
        }
    }

    @Override
    public void render(Game game) {
        onEdt(() -> {
            this.game = game;
            clearSelection();
            if (frame != null) {
                draw();
            }
        });
    }

    @Override
    public void showMessage(String text) {
        onEdt(() -> {
            pendingMessage = text;
            if (message != null) {
                message.setText(text);
                message.setToolTipText(text);
            }
        });
    }

    @Override
    public void start(GameController controller) {
        onEdt(() -> {
            if (frame != null || closed) {
                return;
            }
            this.controller = controller;
            frame = new JFrame("CSC 413 Chess");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    request(new PlayerAction.Quit());
                }
            });
            JPanel board = new JPanel(new GridLayout(8, 8));
            for (int rank = 7; rank >= 0; rank--) {
                for (int file = 0; file < 8; file++) {
                    Position square = new Position(file, rank);
                    JButton button = new JButton();
                    button.setPreferredSize(new Dimension(76, 68));
                    button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
                    button.setOpaque(true);
                    button.setForeground(Color.BLACK);
                    button.getAccessibleContext().setAccessibleName(square.toString());
                    button.addActionListener(event -> click(square));
                    squares[file][rank] = button;
                    board.add(button);
                }
            }
            JPanel information = new JPanel(new GridLayout(0, 1));
            status = new JLabel();
            selection = new JLabel();
            message = new JLabel(pendingMessage);
            message.setToolTipText(pendingMessage);
            information.add(status);
            information.add(new JLabel("White: W + uppercase; Black: B + lowercase. Promotions become queens."));
            information.add(selection);
            information.add(message);
            JPanel controls = new JPanel();
            addControl(controls, "Undo", new PlayerAction.Undo());
            addControl(controls, "Legal moves", new PlayerAction.ShowMoves());
            addControl(controls, "Quit", new PlayerAction.Quit());
            frame.add(information, BorderLayout.NORTH);
            frame.add(board, BorderLayout.CENTER);
            frame.add(controls, BorderLayout.SOUTH);
            if (game != null) {
                draw();
            }
            frame.pack();
            frame.setMinimumSize(frame.getSize());
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    private void addControl(JPanel controls, String label, PlayerAction action) {
        JButton button = new JButton(label);
        button.addActionListener(event -> request(action));
        controls.add(button);
    }

    private void request(PlayerAction action) {
        clearSelection();
        showMessage(" ");
        controller.handle(action);
    }

    private void click(Position square) {
        if (game == null || closed) {
            return;
        }
        Piece piece = game.board().pieceAt(square);
        if (square.equals(source)) {
            clearSelection();
            draw();
        } else if (source == null || (piece != null && piece.color() == game.sideToMove())) {
            List<Move> moves = game.legalMoves().stream()
                    .filter(move -> move.from().equals(square)).toList();
            if (moves.isEmpty()) {
                showMessage("Choose one of your pieces with a legal move.");
                return;
            }
            selectedMoves = moves;
            source = square;
            showMessage("Choose a destination; click the source again to cancel.");
            draw();
        } else {
            request(new PlayerAction.Move(source.toString() + square));
        }
    }

    private void clearSelection() {
        source = null;
        selectedMoves = List.of();
    }

    private void draw() {
        status.setText(game.sideToMove() + " to move | " + game.status());
        selection.setText(source == null ? "Choose a piece." : "Selected " + source
                + " | Legal destinations: " + selectedMoves.stream().map(Move::to)
                .distinct().map(Position::toString).sorted().toList());
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                Position square = new Position(file, rank);
                Piece piece = game.board().pieceAt(square);
                JButton button = squares[file][rank];
                String label = piece == null ? " " :
                        (piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE ? "W " : "B ") + piece.symbol();
                button.setText(label);
                button.setBackground((file + rank) % 2 == 0
                        ? new Color(160, 180, 200) : new Color(245, 240, 220));
                boolean destination = selectedMoves.stream().anyMatch(move -> move.to().equals(square));
                button.setBorder(BorderFactory.createLineBorder(square.equals(source) ? new Color(0, 50, 150)
                        : destination ? new Color(0, 100, 40) : Color.GRAY, square.equals(source) || destination ? 4 : 1));
                String description = square + ": " + (piece == null ? "empty" : piece.color() + " " + piece.type())
                        + (destination ? ", legal destination" : "");
                button.setToolTipText(description);
                button.getAccessibleContext().setAccessibleDescription(description);
            }
        }
    }

    // The BoardView default promotionChoice supplies QUEEN to the controller.
    @Override
    public void close() {
        onEdt(() -> {
            closed = true;
            if (frame != null) {
                frame.dispose();
            }
        });
    }
}

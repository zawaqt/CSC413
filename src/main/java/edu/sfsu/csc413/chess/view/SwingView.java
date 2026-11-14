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
import java.util.Set;
import java.util.stream.Collectors;

/** Event-driven board. All state, game queries and controller actions live on the EDT. */
public final class SwingView implements BoardView {
    private final JButton[][] squares = new JButton[8][8];
    private Game game;
    private GameController controller;
    private JFrame frame;
    private JLabel statusLabel;
    private JLabel messageLabel;
    private Position selected;
    private String message = "Select a piece, then its destination. Promotion defaults to queen.";
    private boolean quitting;

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
            selected = null;
            draw();
        });
    }

    @Override
    public void showMessage(String message) {
        onEdt(() -> {
            this.message = message;
            if (messageLabel != null) {
                messageLabel.setText(message);
            }
        });
    }

    @Override
    public void start(GameController controller) {
        onEdt(() -> {
            this.controller = controller;
            if (frame != null) {
                return;
            }
            frame = new JFrame("CSC 413 Chess");
            frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent event) {
                    quit();
                }
            });
            JPanel board = new JPanel(new GridLayout(8, 8));
            for (int rank = 7; rank >= 0; rank--) {
                for (int file = 0; file < 8; file++) {
                    Position position = new Position(file, rank);
                    JButton square = new JButton();
                    square.setPreferredSize(new Dimension(70, 70));
                    square.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 23));
                    square.setOpaque(true);
                    square.getAccessibleContext().setAccessibleName(position.toString());
                    square.addActionListener(event -> click(position));
                    squares[file][rank] = square;
                    board.add(square);
                }
            }
            statusLabel = new JLabel();
            messageLabel = new JLabel(message);
            JPanel labels = new JPanel(new GridLayout(0, 1));
            labels.add(statusLabel);
            labels.add(new JLabel("Click source, then destination. White: uppercase; Black: lowercase. Queen promotion."));
            labels.add(new JLabel("Gold border: selected square. Green border: legal destination."));
            labels.add(messageLabel);
            JButton undo = new JButton("Undo");
            undo.addActionListener(event -> send(new PlayerAction.Undo()));
            JButton quit = new JButton("Quit");
            quit.addActionListener(event -> quit());
            JPanel controls = new JPanel();
            controls.add(undo);
            controls.add(quit);
            frame.add(labels, BorderLayout.NORTH);
            frame.add(board, BorderLayout.CENTER);
            frame.add(controls, BorderLayout.SOUTH);
            draw();
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    private void click(Position position) {
        if (game == null || quitting || game.status().isGameOver()) {
            return;
        }
        Piece piece = game.board().pieceAt(position);
        if (selected == null) {
            if (piece != null && piece.color() == game.sideToMove()) {
                selected = position;
                showMessage("Selected " + position + ". Click a destination, or click again to cancel.");
            } else {
                showMessage("Select a piece belonging to " + game.sideToMove() + ".");
            }
            draw();
        } else if (selected.equals(position)) {
            selected = null;
            showMessage("Selection cleared.");
            draw();
        } else if (piece != null && piece.color() == game.sideToMove()) {
            selected = position;
            showMessage("Selected " + position + ". Click a destination.");
            draw();
        } else {
            String notation = selected.toString() + position;
            selected = null;
            send(new PlayerAction.Move(notation));
        }
    }

    private void send(PlayerAction action) {
        selected = null;
        showMessage(" ");
        controller.handle(action);
    }

    private void quit() {
        quitting = true;
        controller.handle(new PlayerAction.Quit());
    }

    private void draw() {
        if (frame == null || game == null) {
            return;
        }
        List<Move> legal = game.legalMoves();
        Set<Position> destinations = legal.stream()
                .filter(move -> move.from().equals(selected))
                .map(Move::to).collect(Collectors.toSet());
        statusLabel.setText(game.sideToMove() + " to move | " + game.status());
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Position position = new Position(file, rank);
                Piece piece = game.board().pieceAt(position);
                JButton square = squares[file][rank];
                boolean light = (file + rank) % 2 != 0;
                square.setBackground(light ? new Color(245, 238, 216) : new Color(50, 65, 80));
                square.setForeground(light ? Color.BLACK : Color.WHITE);
                square.setText("<html><center>" + (piece == null ? "&nbsp;" : piece.symbol())
                        + "<br><small>" + position + "</small></center></html>");
                square.setToolTipText(position + ": " + (piece == null ? "empty" : piece.color() + " " + piece.type()));
                square.getAccessibleContext().setAccessibleDescription(square.getToolTipText());
                Color border = position.equals(selected) ? new Color(255, 190, 0)
                        : destinations.contains(position) ? new Color(0, 210, 100)
                        : light ? new Color(245, 238, 216) : new Color(50, 65, 80);
                square.setBorder(BorderFactory.createLineBorder(border, 4));
            }
        }
    }

    /** Keep a finished board visible for review/Undo; explicit Quit disposes it. */
    @Override
    public void close() {
        onEdt(() -> {
            if (frame != null && quitting) {
                frame.dispose();
            }
        });
    }
}

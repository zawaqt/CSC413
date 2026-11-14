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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;
import java.util.stream.Collectors;

/** Event-driven, white-facing board. All state and model queries stay on the EDT. */
public final class SwingView implements BoardView {
    private final JButton[][] squares = new JButton[8][8];
    private Game game;
    private GameController controller;
    private JFrame frame;
    private JLabel statusLabel;
    private JLabel messageLabel;
    private String message = " ";
    private Position selected;
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
                messageLabel.setToolTipText(message);
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
            JPanel coordinates = new JPanel(new GridLayout(10, 10));
            for (int row = 0; row < 10; row++) {
                for (int column = 0; column < 10; column++) {
                    if (row == 0 || row == 9) {
                        coordinates.add(new JLabel(column > 0 && column < 9
                                ? String.valueOf((char) ('a' + column - 1)) : "",
                                SwingConstants.CENTER));
                    } else if (column == 0 || column == 9) {
                        coordinates.add(new JLabel(String.valueOf(9 - row), SwingConstants.CENTER));
                    } else {
                        Position position = new Position(column - 1, 8 - row);
                        JButton square = new JButton();
                        square.setPreferredSize(new Dimension(60, 60));
                        square.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
                        square.setOpaque(true);
                        square.setContentAreaFilled(true);
                        square.getAccessibleContext().setAccessibleName(position.toString());
                        square.addActionListener(event -> click(position));
                        squares[position.file()][position.rank()] = square;
                        coordinates.add(square);
                    }
                }
            }
            JPanel labels = new JPanel(new GridLayout(3, 1));
            statusLabel = new JLabel();
            messageLabel = new JLabel(message);
            labels.add(statusLabel);
            labels.add(messageLabel);
            labels.add(new JLabel("White: uppercase; Black: lowercase. Blue: selected; orange: legal target."));
            JPanel controls = new JPanel();
            JButton undo = new JButton("Undo");
            undo.addActionListener(event -> request(new PlayerAction.Undo()));
            JButton legalMoves = new JButton("Legal moves");
            legalMoves.addActionListener(event -> request(new PlayerAction.ShowMoves()));
            JButton quit = new JButton("Quit");
            quit.addActionListener(event -> quit());
            controls.add(undo);
            controls.add(legalMoves);
            controls.add(quit);
            frame.add(labels, BorderLayout.NORTH);
            frame.add(coordinates, BorderLayout.CENTER);
            frame.add(controls, BorderLayout.SOUTH);
            draw();
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
    }

    private void click(Position position) {
        if (game == null || quitting) {
            return;
        }
        Piece piece = game.board().pieceAt(position);
        if (position.equals(selected)) {
            selected = null;
        } else if (selected == null || (piece != null && piece.color() == game.sideToMove())) {
            selected = piece != null && piece.color() == game.sideToMove() ? position : null;
        } else {
            String notation = selected.toString() + position;
            selected = null;
            // The controller resolves legality and uses BoardView's queen promotion default.
            request(new PlayerAction.Move(notation));
            return;
        }
        draw();
    }

    private void request(PlayerAction action) {
        selected = null;
        showMessage(" ");
        controller.handle(action);
    }

    private void quit() {
        quitting = true;
        request(new PlayerAction.Quit());
    }

    private void draw() {
        if (frame == null || game == null) {
            return;
        }
        Set<Position> destinations = game.legalMoves().stream()
                .filter(move -> move.from().equals(selected))
                .map(Move::to).collect(Collectors.toSet());
        for (int file = 0; file < 8; file++) {
            for (int rank = 0; rank < 8; rank++) {
                Position position = new Position(file, rank);
                JButton square = squares[file][rank];
                Piece piece = game.board().pieceAt(position);
                square.setText(piece == null ? "" : String.valueOf(piece.symbol()));
                // Black text contrasts with both deliberately light square shades.
                square.setForeground(Color.BLACK);
                square.setBackground((file + rank) % 2 == 0
                        ? new Color(172, 188, 162) : new Color(250, 242, 221));
                boolean source = position.equals(selected);
                boolean target = destinations.contains(position);
                square.setBorder(BorderFactory.createLineBorder(source ? new Color(0, 65, 190)
                        : target ? new Color(170, 70, 0) : Color.GRAY, source || target ? 5 : 1));
                String description = position + ": " + (piece == null ? "empty"
                        : piece.color() + " " + piece.type())
                        + (source ? ", selected" : target ? ", legal destination" : "");
                square.setToolTipText(description);
                square.getAccessibleContext().setAccessibleDescription(description);
            }
        }
        statusLabel.setText(game.sideToMove() + " to move | " + game.status());
        messageLabel.setText(message);
        messageLabel.setToolTipText(message);
    }

    @Override
    public void close() {
        onEdt(() -> {
            // The controller also closes on checkmate: keep that final board and
            // result visible, with Undo available, until the player chooses Quit.
            if (quitting && frame != null) {
                frame.dispose();
            }
        });
    }
}

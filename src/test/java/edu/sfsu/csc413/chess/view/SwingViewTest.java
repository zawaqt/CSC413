package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.factory.BoardFactory;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

class SwingViewTest {
    private AbstractButton button(Container parent, String name) {
        for (Component child : parent.getComponents()) {
            if (child instanceof AbstractButton button
                    && (name.equals(button.getAccessibleContext().getAccessibleName())
                    || name.equals(button.getText()))) return button;
            if (child instanceof Container container) {
                AbstractButton found = button(container, name);
                if (found != null) return found;
            }
        }
        return null;
    }

    private boolean containsText(Container parent, String text) {
        for (Component child : parent.getComponents()) {
            if (child instanceof JLabel label && label.getText() != null
                    && label.getText().contains(text)) return true;
            if (child instanceof Container container && containsText(container, text)) return true;
        }
        return false;
    }

    private JFrame start(Game game) {
        new GameController(game, new SwingView()).start();
        for (Frame frame : Frame.getFrames()) {
            if (frame instanceof JFrame chess && frame.isDisplayable()) return chess;
        }
        throw new AssertionError("No window after starting on the EDT");
    }

    private void click(JFrame frame, String name) {
        AbstractButton target = button(frame, name);
        assertNotNull(target, name);
        target.doClick(0);
    }

    private void move(JFrame frame, String notation) {
        click(frame, notation.substring(0, 2));
        click(frame, notation.substring(2, 4));
    }

    private void disposeWindows() {
        for (Frame frame : Frame.getFrames()) frame.dispose();
    }

    @Test
    void clickActionsRejectUndoListAndFinish() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Swing integration needs a display");
        SwingUtilities.invokeAndWait(() -> {
            try {
                Game game = new Game();
                JFrame frame = start(game);
                move(frame, "e2e5");
                assertTrue(game.history().isEmpty());
                assertTrue(containsText(frame, "Not a legal move: 'e2e5'."));
                move(frame, "e2e4");
                assertEquals(1, game.history().size());
                click(frame, "Undo");
                assertTrue(game.history().isEmpty());
                click(frame, "Legal moves");
                assertTrue(containsText(frame, "20 legal moves:"));
                for (String notation : new String[] {"f2f3", "e7e5", "g2g4", "d8h4"}) {
                    move(frame, notation);
                }
                assertEquals(GameStatus.CHECKMATE, game.status());
                assertTrue(containsText(frame, "Checkmate. BLACK wins."));
                assertTrue(frame.isDisplayable(), "Final result remains visible");
                click(frame, "Quit");
                assertFalse(frame.isDisplayable());
            } finally {
                disposeWindows();
            }
        });
    }

    @Test
    void promotionUsesTheControllersQueenDefault() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Swing integration needs a display");
        SwingUtilities.invokeAndWait(() -> {
            try {
                Game game = new Game(BoardFactory.fromFen("k7/4P3/8/8/8/8/8/4K3"), Color.WHITE);
                JFrame frame = start(game);
                move(frame, "e7e8");
                assertEquals(PieceType.QUEEN, game.board().pieceAt(Position.parse("e8")).type());
            } finally {
                disposeWindows();
            }
        });
    }
}

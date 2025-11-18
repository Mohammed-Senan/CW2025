package com.comp2042;

import com.comp2042.app.Main;
import com.comp2042.event.EventSource;
import com.comp2042.event.EventType;
import com.comp2042.event.MoveEvent;
import com.comp2042.logic.Board;
import com.comp2042.logic.GameController;
import com.comp2042.logic.Score;
import com.comp2042.logic.SimpleBoard;
import com.comp2042.ui.GuiController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameTest {

    @Mock
    private SimpleBoard mockBoard;

    @Mock
    private Score mockScore;

    @Mock
    private GuiController mockGui;

    private GameController gameController;

    @BeforeEach
    public void setUp() {
        when(mockBoard.getScore()).thenReturn(mockScore);
        gameController = new GameController(mockGui, mockBoard);
    }

    @Test
    public void testSoftDropAddsCorrectScore() {
        MoveEvent softDropEvent = new MoveEvent(EventType.DOWN, EventSource.USER);
        when(mockBoard.moveBrickDown()).thenReturn(true);

        gameController.onDownEvent(softDropEvent);

        verify(mockScore).add(1);
    }
    @Test
    public void testLineClearingAddsCorrectScore() {
        int[][] emptyMatrix = new int[10][20];
        com.comp2042.logic.ClearRow fakeClearRow = new com.comp2042.logic.ClearRow(2, emptyMatrix);

        when(mockBoard.moveBrickDown()).thenReturn(false);
        when(mockBoard.clearRows()).thenReturn(fakeClearRow);

        gameController.onDownEvent(new MoveEvent(com.comp2042.event.EventType.DOWN, com.comp2042.event.EventSource.USER));

        verify(mockScore).add(200);
    }
}
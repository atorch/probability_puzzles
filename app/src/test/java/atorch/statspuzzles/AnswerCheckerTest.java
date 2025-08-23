package atorch.statspuzzles;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnswerCheckerTest {

    @Test
    public void testCorrectAnswer() {
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("2+2", "4"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/2", "0.5"));
    }

    @Test
    public void testInaccurateAnswer() {
        assertEquals(AnswerChecker.Result.INACCURATE, AnswerChecker.checkAnswer("1/3", "0.333"));
    }

    @Test
    public void testIncorrectAnswer() {
        assertEquals(AnswerChecker.Result.INCORRECT, AnswerChecker.checkAnswer("1", "2"));
    }

    @Test
    public void testInvalidAnswer() {
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("1", "abc"));
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("1", ""));
    }
}

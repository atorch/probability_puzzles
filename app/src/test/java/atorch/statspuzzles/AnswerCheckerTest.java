package atorch.statspuzzles;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnswerCheckerTest {

    @Test
    public void testCorrectAnswer() {
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("2+2", "4"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/2", "0.5"));
	assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("3!", "6.0"));
	// Note that we have a Result.INACCURATE version of this test as well
	assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/3", "0.33333333333333"));
	assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("C(5, 3)", "10"));
    }

    @Test
    public void testInaccurateAnswer() {
        assertEquals(AnswerChecker.Result.INACCURATE, AnswerChecker.checkAnswer("1/3", "0.333"));
    }

    @Test
    public void testIncorrectAnswer() {
        assertEquals(AnswerChecker.Result.INCORRECT, AnswerChecker.checkAnswer("1", "2"));
        // The user's answer is not close enough to be considered merely inaccurate.
        assertEquals(AnswerChecker.Result.INCORRECT, AnswerChecker.checkAnswer("1/3", "0.33"));
    }

    @Test
    public void testInvalidAnswer() {
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("1", "abc"));
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("1", ""));
	// Imbalanced parentheses
	assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("( 1 + 5", ""));
    }
}

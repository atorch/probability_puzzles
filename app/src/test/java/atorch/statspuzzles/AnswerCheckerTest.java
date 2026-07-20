package atorch.statspuzzles;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnswerCheckerTest {

    @Test
    public void testCorrectAnswer() {
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("2 + 2", "4"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/ 2", "0.5"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("3!", "6.0"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/(3!)", "0.16666666"));
        // Note that we have a Result.INACCURATE version of this test as well
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/3", "0.33333333333333"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("C(5, 3)", "10"));
    }

    @Test
    public void testCombinations() {
        // Test various combination expressions that users might enter
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("C(8, 6)", "28"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("C(10, 2)", "45"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("C(52, 5)", "2598960"));
    }

    @Test
    public void testEulersNumber() {
        // Test expressions involving e
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/e", "0.36787944"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("e^2", "7.389056"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1-1/e", "0.63212056"));
    }

    @Test
    public void testNegativeExponents() {
        // Test negative exponents that users might enter
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("4^-1", "0.25"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("2^-3", "0.125"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("10 ^ -2", "0.01"));
    }

    @Test
    public void testPercentages() {
        // Test percentage expressions
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("50%", "0.5"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("25%", "0.25"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("100%", "1"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1%", "0.01"));
    }

    @Test
    public void testSpacingAndEquivalentExpressions() {
        // Test that spacing variations and mathematically equivalent expressions are accepted
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("C(7,3)", "C(7, 3)"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("C(7, 3)", "C(7,4)"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("4!", "4 * 3!"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("12!/(4!*4!*4!)", "12! / ((4!)^3)"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/2 + (1/2)*49/99", "1/2+(49/2) * 1/99"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("(4/52)*(3/51)", "( 4 * 3 ) / (51 *52)"));
    }

    @Test
    public void testEquivalentFormats() {
        // Test that the app accepts equivalent formats as documented in the intro
        // .2, 0.2, 20%, and 1/5 should all be equivalent
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("0.2", ".2"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("0.2", "20%"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("0.2", "1/5"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("1/5", "20%"));

        // .4, 0.4, 40%, 4/10, and 2*1/5 should all be equivalent
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("0.4", ".4"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("0.4", "40%"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("0.4", "4/10"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("0.4", "2*1/5"));

        // Factorial equivalents: 4! = 4*3*2 = 24
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("4!", "4*3*2"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("4!", "24"));

        // Combination equivalents: C(5,3) = 5!/(3!*2!)
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("C(5,3)", "5!/(3!*2!)"));

        // Exponent equivalents: .49^3 = .49*.49*.49
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer(".49^3", ".49*.49*.49"));
    }

    @Test
    public void testPiConstant() {
        // Test that pi constant works
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("pi", "3.14159265"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("pi / 4", "0.78539816"));
        assertEquals(AnswerChecker.Result.CORRECT, AnswerChecker.checkAnswer("2*pi", "6.28318531"));
    }

    @Test
    public void testInaccurateAnswer() {
        assertEquals(AnswerChecker.Result.INACCURATE, AnswerChecker.checkAnswer("1/3", "0.333"));
    }

    @Test
    public void testIncorrectAnswer() {
        assertEquals(AnswerChecker.Result.INCORRECT, AnswerChecker.checkAnswer("1", "2"));
	assertEquals(AnswerChecker.Result.INCORRECT, AnswerChecker.checkAnswer("0.50", "0.49"));
        // The user's answer is not close enough to be considered merely inaccurate.
        assertEquals(AnswerChecker.Result.INCORRECT, AnswerChecker.checkAnswer("1/3", "0.33"));
    }

    @Test
    public void testInvalidAnswer() {
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("1", "abc"));
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("1", ""));
        // Imbalanced parentheses
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("( 1 + 5", ""));
        // Undefined, should be invalid
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("1", "0/0"));
        assertEquals(AnswerChecker.Result.INVALID, AnswerChecker.checkAnswer("1", "hello world, this is not math"));
    }
}

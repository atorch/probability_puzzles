package atorch.statspuzzles;

import org.mariuszgromada.math.mxparser.Expression;

public class AnswerChecker {

    public enum Result {
        CORRECT,
        INACCURATE,
        INCORRECT,
        INVALID
    }

    public static Result checkAnswer(String correctAnswerExpression, String userAnswerExpression) {
        Expression correctAnswer = new Expression(correctAnswerExpression);
        Expression userAnswer = new Expression(userAnswerExpression);

        if (!userAnswer.checkSyntax()) {
            return Result.INVALID;
        }

        double correctAnswerValue = correctAnswer.calculate();
        double userAnswerValue = userAnswer.calculate();

        if (Double.isNaN(userAnswerValue) || Double.isInfinite(userAnswerValue)) {
            return Result.INVALID;
        }

        if (Math.abs(userAnswerValue - correctAnswerValue) < 0.00001) {
            return Result.CORRECT;
        } else if (Math.abs(userAnswerValue - correctAnswerValue) < 0.001) {
            return Result.INACCURATE;
        } else {
            return Result.INCORRECT;
        }
    }
}

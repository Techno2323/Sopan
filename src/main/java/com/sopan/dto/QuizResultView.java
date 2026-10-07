package com.sopan.dto;

import com.sopan.model.QuizAttempt;
import com.sopan.model.enums.MasteryLevel;

import java.util.List;

public class QuizResultView {

    public static class QuestionFeedback {
        private final String prompt;
        private final String conceptTitle;
        private final int conceptId;
        private final String selectedOption;
        private final String correctOption;
        private final boolean correct;
        private final double updatedMastery;
        private final MasteryLevel masteryLevel;

        public QuestionFeedback(String prompt, String conceptTitle, int conceptId,
                                String selectedOption, String correctOption,
                                boolean correct, double updatedMastery,
                                MasteryLevel masteryLevel) {
            this.prompt = prompt;
            this.conceptTitle = conceptTitle;
            this.conceptId = conceptId;
            this.selectedOption = selectedOption;
            this.correctOption = correctOption;
            this.correct = correct;
            this.updatedMastery = updatedMastery;
            this.masteryLevel = masteryLevel;
        }

        public String getPrompt() { return prompt; }
        public String getConceptTitle() { return conceptTitle; }
        public int getConceptId() { return conceptId; }
        public String getSelectedOption() { return selectedOption; }
        public String getCorrectOption() { return correctOption; }
        public boolean isCorrect() { return correct; }
        public double getUpdatedMastery() { return updatedMastery; }
        public MasteryLevel getMasteryLevel() { return masteryLevel; }
    }

    private final QuizAttempt attempt;
    private final String quizTitle;
    private final List<QuestionFeedback> questions;

    public QuizResultView(QuizAttempt attempt, String quizTitle, List<QuestionFeedback> questions) {
        this.attempt = attempt;
        this.quizTitle = quizTitle;
        this.questions = questions;
    }

    public QuizAttempt getAttempt() { return attempt; }
    public String getQuizTitle() { return quizTitle; }
    public List<QuestionFeedback> getQuestions() { return questions; }
    public int getScore() { return attempt.getScore(); }
    public int getMaxScore() { return attempt.getMaxScore(); }
    public double getPercentage() { return attempt.getPercentage(); }
}

package com.techexchange.mobileapps.lab9;

class Question {

    private final String question, wrongAnswer, correctAnswer;

    public Question(String q, String c, String w) {
        this.question=q;
        this.wrongAnswer=w;
        this.correctAnswer=c;
    }

    public String getQuestion() {
        return question;
    }

    public String getWrongAnswer() {
        return wrongAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }
}

package com.upstream.datamining;


public class Question {
    private int questionId;
    public enum Answer {CORRECT, WRONG, INVALID, UNKNOWN};
    private Answer answer;
    
    public Question(int id, Answer a) {
        this.questionId = id;
        this.answer = a;
    }
    
    public Question(int id, String answerString) {
        this.questionId = id;
        Answer a = Answer.UNKNOWN;
        if ((answerString.equalsIgnoreCase("WRONG")) || (answerString.equalsIgnoreCase("\"WRONG\""))) {
            a = Answer.WRONG;
        }
        else if ((answerString.equalsIgnoreCase("CORRECT")) || (answerString.equalsIgnoreCase("\"CORRECT\""))) {
            a = Answer.CORRECT;
        }
        else if ((answerString.equalsIgnoreCase("INVALID")) || (answerString.equalsIgnoreCase("\"INVALID\""))) {
            a = Answer.INVALID;
        }
        this.answer = a;
    }
    
    public Answer getAnswer() {
        return this.answer;
    }
    
    public int getId() {
        return this.questionId;
    }
    
    public String toString() {
    	String s = "QUESTION: " + questionId + " ANSWER: " + answer.toString();
    	return s;
    }
}

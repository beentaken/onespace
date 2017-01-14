package com.sesame.onespace.models.qaMessage;

import java.util.ArrayList;

/**
 * Created by Thian on 21/12/2559.
 */

public final class QAMessage {

    //===========================================================================================================//
    //  ATTRIBUTE                                                                                   ATTRIBUTE
    //===========================================================================================================//

    private Integer id;
    private String msgFrom;
    private String type;
    private String questionID;
    private String questionStr;
    private ArrayList<String> answerIDList;
    private ArrayList<String> answerStrList;
    private String date;

    //===========================================================================================================//
    //  CONSTRUCTOR                                                                                 CONSTRUCTOR
    //===========================================================================================================//

    public QAMessage(Integer id, String msgFrom, String type, String questionID, String questionStr, ArrayList<String> answerIDList, ArrayList<String> answerStrList, String date){

        QAMessage.this.id = id;
        QAMessage.this.msgFrom = msgFrom;
        QAMessage.this.type = type;
        QAMessage.this.questionID = questionID;
        QAMessage.this.questionStr = questionStr;
        QAMessage.this.answerIDList = answerIDList;
        QAMessage.this.answerStrList = answerStrList;
        QAMessage.this.date = date;

    }

    public QAMessage(String msgFrom, String type, String questionID, String questionStr, ArrayList<String> answerIDList, ArrayList<String> answerStrList, String date){

        QAMessage.this.id = null;
        QAMessage.this.msgFrom = msgFrom;
        QAMessage.this.type = type;
        QAMessage.this.questionID = questionID;
        QAMessage.this.questionStr = questionStr;
        QAMessage.this.answerIDList = answerIDList;
        QAMessage.this.answerStrList = answerStrList;
        QAMessage.this.date = date;

    }

    //===========================================================================================================//
    //  SET&GET                                                                                     SET&GET
    //===========================================================================================================//

    public void setId(Integer id){

        QAMessage.this.id = id;

    }

    public void setMsgFrom(String msgFrom){

        QAMessage.this.msgFrom = msgFrom;

    }

    public void setType(String type){

        QAMessage.this.type = type;

    }

    public void setQuestionID(String questionID){

        QAMessage.this.questionID = questionID;

    }

    public void setQuestionStr(String questionStr){

        QAMessage.this.questionStr = questionStr;

    }

    public void setAnswerIDList(ArrayList<String> answerIDList){

        QAMessage.this.answerIDList = answerIDList;

    }

    public void setAnswerStrList(ArrayList<String> answerStrList){

        QAMessage.this.answerStrList = answerStrList;

    }

    public void setDate(String date){

        QAMessage.this.date = date;

    }

    public Integer getId(){

        return QAMessage.this.id;

    }

    public String getMsgFrom(){

        return QAMessage.this.msgFrom;

    }

    public String getType(){

        return QAMessage.this.type;

    }

    public String getQuestionID(){

        return QAMessage.this.questionID;

    }

    public String getQuestionStr(){

        return QAMessage.this.questionStr;

    }

    public ArrayList<String> getAnswerIDList(){

        return QAMessage.this.answerIDList;

    }

    public ArrayList<String> getAnswerStrList(){

        return QAMessage.this.answerStrList;

    }

    public String getDate(){

        return QAMessage.this.date;

    }

}

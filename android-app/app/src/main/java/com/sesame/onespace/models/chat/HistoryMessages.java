package com.sesame.onespace.models.chat;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chongos on 9/24/15 AD.
 */
public class HistoryMessages {

    @SerializedName("fromjid")
    @Expose
    private String fromjid;
    @SerializedName("fromjidresource")
    @Expose
    private String fromjidresource;
    @SerializedName("tojid")
    @Expose
    private String tojid;
    @SerializedName("tojidresource")
    @Expose
    private String tojidresource;
    @SerializedName("messages")
    @Expose
    private List<Message> messages = new ArrayList<Message>();

    /**
     *
     * @return
     * The fromjid
     */
    public String getFromjid() {
        return fromjid;
    }

    /**
     *
     * @param fromjid
     * The fromjid
     */
    public void setFromjid(String fromjid) {
        this.fromjid = fromjid;
    }

    /**
     *
     * @return
     * The fromjidresource
     */
    public String getFromjidresource() {
        return fromjidresource;
    }

    /**
     *
     * @param fromjidresource
     * The fromjidresource
     */
    public void setFromjidresource(String fromjidresource) {
        this.fromjidresource = fromjidresource;
    }

    /**
     *
     * @return
     * The tojid
     */
    public String getTojid() {
        return tojid;
    }

    /**
     *
     * @param tojid
     * The tojid
     */
    public void setTojid(String tojid) {
        this.tojid = tojid;
    }

    /**
     *
     * @return
     * The tojidresource
     */
    public String getTojidresource() {
        return tojidresource;
    }

    /**
     *
     * @param tojidresource
     * The tojidresource
     */
    public void setTojidresource(String tojidresource) {
        this.tojidresource = tojidresource;
    }

    /**
     *
     * @return
     * The messages
     */
    public List<Message> getMessages() {
        return messages;
    }

    /**
     *
     * @param messages
     * The messages
     */
    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }


    public class Message {

        @SerializedName("sentDate")
        @Expose
        private Long sentDate;
        @SerializedName("stanza")
        @Expose
        private String stanza;
        @SerializedName("source")
        @Expose
        private String source;

        /**
         *
         * @return
         * The sentDate
         */
        public Long getSentDate() {
            return sentDate/1000L;
        }

        /**
         *
         * @param sentDate
         * The sentDate
         */
        public void setSentDate(Long sentDate) {
            this.sentDate = sentDate;
        }

        /**
         *
         * @return
         * The stanza
         */
        public String getStanza() {
            return stanza;
        }

        /**
         *
         * @param stanza
         * The stanza
         */
        public void setStanza(String stanza) {
            this.stanza = stanza;
        }

        /**
         *
         * @return
         * The source
         */
        public String getSource() {
            return source;
        }

        /**
         *
         * @param source
         * The source
         */
        public void setSource(String source) {
            this.source = source;
        }

    }

}
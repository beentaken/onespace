package com.sesame.onespace.models;

/**
 * Created by chongos on 9/3/15 AD.
 */
public class User {

    public enum Type {
        WALKER, SURFER
    }

    private String name;
    private Type type;

    public User(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

}

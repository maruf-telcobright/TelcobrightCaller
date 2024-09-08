package com.example.telcobrightcaller;

public class CallRecord {

    String id;
    String name;
    String number;
    String photo;

    public CallRecord(String id, String name, String number, String photo) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.photo = photo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }
}

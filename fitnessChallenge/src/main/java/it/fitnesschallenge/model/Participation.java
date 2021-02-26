package it.fitnesschallenge.model;

import java.util.ArrayList;

public class Participation {

    private ArrayList<String> roomsList;

    public Participation() {
        // Necessario per deserializzazione DB
    }

    public Participation(ArrayList<String> roomsList) {
        this.roomsList = roomsList;
    }

    public ArrayList<String> getRoomsList() {
        return roomsList;
    }

    public void setRoomsList(ArrayList<String> roomsList) {
        this.roomsList = roomsList;
    }
}

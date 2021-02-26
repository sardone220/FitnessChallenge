package it.fitnesschallenge.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Room implements Parcelable {

    private String idCode;
    private String roomName;
    private String roomCreator;
    private ArrayList<String> members;

    public Room() {
        // necessario per deserializzazione FireBase
        members = new ArrayList<>();
    }

    public Room(Parcel in) {
        idCode = in.readString();
        roomName = in.readString();
        roomCreator = in.readString();
        members = in.readArrayList(String.class.getClassLoader());
    }

    public Room(String idCode, String roomName, String roomCreator) {
        this.idCode = idCode;
        this.roomName = roomName;
        this.roomCreator = roomCreator;
        this.members = new ArrayList<>();
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomCreator() {
        return roomCreator;
    }

    public void setRoomCreator(String roomCreator) {
        this.roomCreator = roomCreator;
    }

    public String getIdCode() {
        return idCode;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    public void addMembers(String member) {
        if (member == null)
            members = new ArrayList<>();
        members.add(member);
    }

    public List<String> getMembers() {
        return members;
    }

    public String getMemberAt(int position) {
        return members.get(position);
    }

    public boolean removeMemberAt(int position) {
        return members.remove(members.get(position));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(idCode);
        dest.writeString(roomName);
        dest.writeString(roomCreator);
        dest.writeList(members);
    }

    public static final Parcelable.Creator<Room> CREATOR
            = new Parcelable.Creator<Room>() {
        public Room createFromParcel(Parcel in) {
            return new Room(in);
        }

        public Room[] newArray(int size) {
            return new Room[size];
        }
    };
}

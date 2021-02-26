package it.fitnesschallenge.model;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String username;
    private String nome;
    private String surname;
    private String role;

    private User(){
        //necessario per fire base
    }

    public User(String username, String nome, String surname, String role) {
        this.username = username;
        this.nome = nome;
        this.surname = surname;
        this.role = role;
    }

    private User(Parcel in){
        this.username = in.readString();
        this.nome = in.readString();
        this.surname = in.readString();
        this.role = in.readString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(nome);
        dest.writeString(surname);
        dest.writeString(role);
    }

    public static final Parcelable.Creator<User> CREATOR
            = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };
}

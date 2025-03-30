package de.secretj12.turnierplaner.model.user;

import de.secretj12.turnierplaner.db.entities.Player;
import de.secretj12.turnierplaner.enums.Language;
import de.secretj12.turnierplaner.enums.Sex;

import java.time.LocalDate;

public class jUserPlayerRegistrationForm {

    String firstName;
    String lastName;
    Sex sex;
    LocalDate birthday;
    String email;
    String phone;
    Language language;

    public jUserPlayerRegistrationForm() {
    }

    public jUserPlayerRegistrationForm(Player player) {
        this.firstName = player.getFirstName();
        this.lastName = player.getLastName();
        this.sex = player.getSex();
        this.birthday = player.getBirthday();
        this.email = player.getEmail();
        this.phone = player.getPhone();
        this.language = player.getLanguage();
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}

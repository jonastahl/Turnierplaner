package de.secretj12.turnierplaner.db.entities;

import de.secretj12.turnierplaner.enums.Language;
import de.secretj12.turnierplaner.enums.Sex;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "players")
@NamedQueries({
               @NamedQuery(name = "filter",
                           query = """
                               SELECT p FROM Player p
                               WHERE p.mailVerified = true
                               AND (p.adminVerified = true OR :admin = true)
                               AND (p.sex = :sex OR :ignoreSex = true)
                               AND (p.birthday <= :minAge OR :ignoreMinAge = true)
                               AND (p.birthday >= :maxAge OR :ignoreMaxAge = true)
                               AND (lower(p.firstName) like CONCAT('%', lower(:search), '%')
                               OR lower(p.lastName) = CONCAT('%', lower(:search), '%')
                               OR lower(CONCAT(p.firstName, ' ', p.lastName)) like CONCAT('%', lower(:search), '%'))
                               ORDER BY CASE
                                   WHEN lower(p.firstName) like CONCAT(lower(:search), '%') THEN 0
                                   WHEN lower(p.lastName) like CONCAT(lower(:search), '%') THEN 1
                                   ELSE 2
                               END, p.firstName, p.lastName"""
               ),
               @NamedQuery(name = "countFilter",
                           query = """
                               SELECT p FROM Player p
                               WHERE p.mailVerified = true
                               AND (p.adminVerified = true OR :admin = true)
                               AND (p.sex = :sex OR :ignoreSex = true)
                               AND (p.birthday <= :minAge OR :ignoreMinAge = true)
                               AND (p.birthday >= :maxAge OR :ignoreMaxAge = true)
                               AND (lower(p.firstName) like CONCAT('%', lower(:search), '%')
                               OR lower(p.lastName) = CONCAT('%', lower(:search), '%')
                               OR lower(CONCAT(p.firstName, ' ', p.lastName)) like CONCAT('%', lower(:search), '%'))"""
               ),
               @NamedQuery(name = "adminUnverified",
                           query = """
                               SELECT p FROM Player p
                               WHERE p.adminVerified = false""")
})
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "sex")
    private Sex sex;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "mail_verified")
    private boolean mailVerified;

    @Column(name = "admin_verified")
    private boolean adminVerified;

    @Column(name = "language")
    private Language language;

    @OneToOne(mappedBy = "player", cascade = CascadeType.REMOVE)
    private VerificationCode verificationCode;

    public Player() {
    }

    public Player(String firstName, String lastName, Sex sex, LocalDate birthday, String email, String phone,
                  boolean mailVerified, boolean adminVerified, Language language) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.sex = sex;
        this.birthday = birthday;
        this.email = email;
        this.phone = phone;
        this.mailVerified = mailVerified;
        this.adminVerified = adminVerified;
        this.language = language;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
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

    public boolean isMailVerified() {
        return mailVerified;
    }

    public void setMailVerified(boolean mail_verified) {
        this.mailVerified = mail_verified;
    }

    public boolean isAdminVerified() {
        return adminVerified;
    }

    public void setAdminVerified(boolean admin_verified) {
        this.adminVerified = admin_verified;
    }

    public VerificationCode getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(VerificationCode verificationCodes) {
        this.verificationCode = verificationCodes;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }
}

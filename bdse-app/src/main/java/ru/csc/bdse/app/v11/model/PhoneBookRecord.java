package ru.csc.bdse.app.v11.model;

import org.jetbrains.annotations.NotNull;
import ru.csc.bdse.app.Record;
import ru.csc.bdse.app.proto.PhoneBookRecordProto;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class PhoneBookRecord implements Record {

    @NotNull
    public final String firstName;
    @NotNull
    public final String lastName;
    public final String phoneNumber;

    private PhoneBookRecord(@NotNull String firstName, @NotNull String lastName, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    public static PhoneBookRecord newRecord(@NotNull String firstName, @NotNull String lastName, String phoneNumber) {
        return new PhoneBookRecord(firstName, lastName, phoneNumber);
    }

    public static PhoneBookRecord newRecord(@NotNull PhoneBookRecordProto.PhoneBookRecord record) {
        return new PhoneBookRecord(record.getFirstName(),
                record.getLastName(),
                record.getPhoneNumberCount() > 0 ? record.getPhoneNumber(0) : null);
    }

    public static PhoneBookRecord newRecord(@NotNull String firstName, @NotNull String lastName) {
        return new PhoneBookRecord(firstName, lastName, null);
    }

    @Override
    @NotNull
    public Set<Character> literals() {
        return Collections.singleton(lastName.charAt(0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneBookRecord that = (PhoneBookRecord) o;
        return Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(phoneNumber, that.phoneNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, phoneNumber);
    }

    @Override
    public String toString() {
        return "PhoneBookRecord{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}

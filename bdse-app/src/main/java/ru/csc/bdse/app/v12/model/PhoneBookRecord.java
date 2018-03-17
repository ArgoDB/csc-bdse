package ru.csc.bdse.app.v12.model;

import org.jetbrains.annotations.NotNull;
import ru.csc.bdse.app.Record;
import ru.csc.bdse.app.proto.PhoneBookRecordProto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PhoneBookRecord implements Record {

    @NotNull
    public final String firstName;
    @NotNull
    public final String lastName;
    public final String nickname;
    @NotNull
    public final List<String> phoneNumbers;

    private PhoneBookRecord(@NotNull String firstName, @NotNull String lastName, String nickname, @NotNull List<String> phoneNumbers) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.phoneNumbers = phoneNumbers;
    }

    public static PhoneBookRecord newRecord(@NotNull String firstName,
                                            @NotNull String lastName,
                                            String nickname,
                                            @NotNull List<String> phoneNumbers) {
        return new PhoneBookRecord(firstName, lastName, nickname, phoneNumbers);
    }

    public static PhoneBookRecord newRecord(@NotNull PhoneBookRecordProto.PhoneBookRecord record) {
        return new PhoneBookRecord(
                record.getFirstName(),
                record.getLastName(),
                record.getNickname(),
                record.getPhoneNumberList());
    }

    public static PhoneBookRecord newRecord(@NotNull String firstName, @NotNull String lastName) {
        return new PhoneBookRecord(firstName, lastName, null, Collections.emptyList());
    }

    @Override
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
                Objects.equals(nickname, that.nickname) &&
                Objects.equals(phoneNumbers, that.phoneNumbers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, nickname, phoneNumbers);
    }

    @Override
    public String toString() {
        return "PhoneBookRecord{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", nickname='" + nickname + '\'' +
                ", phoneNumbers=" + phoneNumbers +
                '}';
    }
}

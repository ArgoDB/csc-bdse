package ru.csc.bdse.app.util;

import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.util.StringUtils;
import ru.csc.bdse.app.proto.PhoneBookRecordProto;

public final class ProtoUtils {

    public static byte[] encode(ru.csc.bdse.app.v11.model.PhoneBookRecord record) {
        return PhoneBookRecordProto.PhoneBookRecord.newBuilder()
                .setFirstName(record.firstName)
                .setLastName(record.lastName)
                .addPhoneNumber(record.phoneNumber)
                .build()
                .toByteArray();
    }

    public static byte[] encode(ru.csc.bdse.app.v12.model.PhoneBookRecord record) {
        final PhoneBookRecordProto.PhoneBookRecord.Builder builder =  PhoneBookRecordProto.PhoneBookRecord.newBuilder();
        builder.setFirstName(record.firstName)
                .setLastName(record.lastName);
        if (!StringUtils.isEmpty(record.nickname)) {
            builder.setNickname(record.nickname);
        }
        for (String phoneNumber : record.phoneNumbers) {
            builder.addPhoneNumber(phoneNumber);
        }
        return builder.build().toByteArray();
    }

    public static ru.csc.bdse.app.v11.model.PhoneBookRecord decodeV11(byte[] bytes) throws InvalidProtocolBufferException {
        return ru.csc.bdse.app.v11.model.PhoneBookRecord.newRecord(PhoneBookRecordProto.PhoneBookRecord.parseFrom(bytes));
    }

    public static ru.csc.bdse.app.v12.model.PhoneBookRecord decodeV12(byte[] bytes) throws InvalidProtocolBufferException {
        return ru.csc.bdse.app.v12.model.PhoneBookRecord.newRecord(PhoneBookRecordProto.PhoneBookRecord.parseFrom(bytes));
    }
}

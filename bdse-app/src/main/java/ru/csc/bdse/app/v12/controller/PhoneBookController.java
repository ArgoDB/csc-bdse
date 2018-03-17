package ru.csc.bdse.app.v12.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.app.PhoneBookApi;
import ru.csc.bdse.app.v12.model.PhoneBookRecord;

import java.util.List;
import java.util.Set;

@RestController("phoneBookControllerV12")
public class PhoneBookController {

    private final PhoneBookApi<PhoneBookRecord> phoneBook;

    @Autowired
    public PhoneBookController(@Qualifier("phoneBookV12") PhoneBookApi<PhoneBookRecord> phoneBook) {
        this.phoneBook = phoneBook;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/putV2")
    public void put(@RequestParam("firstName") String firstName,
                      @RequestParam("lastName") String lastName,
                      @RequestParam(value = "nickname", required = false) String nickName,
                      @RequestParam("phoneNumbers") List<String> phoneNumbers) {
        phoneBook.put(PhoneBookRecord.newRecord(firstName, lastName, nickName, phoneNumbers));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/delV2")
    public void delete(@RequestParam("firstName") String firstName,
                         @RequestParam("lastName") String lastName) {
        phoneBook.delete(PhoneBookRecord.newRecord(firstName, lastName));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getV2")
    public Set<PhoneBookRecord> get(@RequestParam("literal") char literal) {
        return phoneBook.get(literal);
    }
}
package ru.csc.bdse.app.v11.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.csc.bdse.app.PhoneBookApi;
import ru.csc.bdse.app.v11.model.PhoneBookRecord;

import java.util.Set;

@RestController("phoneBookControllerV11")
public class PhoneBookController {

    private final PhoneBookApi<PhoneBookRecord> phoneBook;

    @Autowired
    public PhoneBookController(@Qualifier("phoneBookV11") PhoneBookApi<PhoneBookRecord> phoneBook) {
        this.phoneBook = phoneBook;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/putV1")
    public void put(@RequestParam("firstName") String firstName,
                      @RequestParam("lastName") String lastName,
                      @RequestParam("phoneNumber") String phoneNumber) {
        phoneBook.put(PhoneBookRecord.newRecord(firstName, lastName, phoneNumber));
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/delV1")
    public void delete(@RequestParam("firstName") String firstName,
                         @RequestParam("lastName") String lastName) {
        phoneBook.delete(PhoneBookRecord.newRecord(firstName, lastName));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/getV1")
    public Set<PhoneBookRecord> get(@RequestParam("literal") char literal) {
        return phoneBook.get(literal);
    }
}

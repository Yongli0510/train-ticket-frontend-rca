package contacts.controller;

import contacts.entity.*;
import edu.fudan.common.util.Response;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import contacts.service.ContactsService;

import javax.annotation.PostConstruct;
import java.util.UUID;

import static org.springframework.http.ResponseEntity.ok;

/**
 * @author fdse
 */
@RestController
@RequestMapping("api/v1/contactservice")
public class ContactsController {
    @Autowired
    private ContactsService contactsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactsController.class);


    private Counter getAllContacts_error;
    private Counter createNewContacts_error;
    private Counter createNewContactsAdmin_error;
    private Counter deleteContacts_error;
    private Counter modifyContacts_error;
    private Counter findContactsByAccountId_error;
    private Counter getContactsByContactsId_error;


    @Autowired
    private MeterRegistry meterRegistry;

    @PostConstruct
    public void init() {
        Tags tags = Tags.of("service", "ts-contacts-service");
        meterRegistry.config().commonTags(tags);
        getAllContacts_error = Counter.builder("ts.contacts.getAllContacts.error").register(meterRegistry);
        createNewContacts_error = Counter.builder("ts.contacts.createNewContacts.error").register(meterRegistry);
        createNewContactsAdmin_error = Counter.builder("ts.contacts.createNewContactsAdmin.error").register(meterRegistry);
        deleteContacts_error = Counter.builder("ts.contacts.deleteContacts.error").register(meterRegistry);
        modifyContacts_error = Counter.builder("ts.contacts.modifyContacts.error").register(meterRegistry);
        findContactsByAccountId_error = Counter.builder("ts.contacts.findContactsByAccountId.error").register(meterRegistry);
        getContactsByContactsId_error = Counter.builder("ts.contacts.getContactsByContactsId.error").register(meterRegistry);
    }

    @GetMapping(path = "/contacts/welcome")
    public String home() {
        return "Welcome to [ Contacts Service ] !";
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/contacts")
    @Timed(value = "ts.contacts.getAllContacts")
    public HttpEntity getAllContacts(@RequestHeader HttpHeaders headers) {
        ContactsController.LOGGER.info("[getAllContacts][Get All Contacts]");
        Response<?> response = contactsService.getAllContacts(headers);
        if (response.getStatus() != 1)
            getAllContacts_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/contacts")
    @Timed(value = "ts.contacts.createNewContacts")
    public ResponseEntity<Response> createNewContacts(@RequestBody Contacts aci,
                                                      @RequestHeader HttpHeaders headers) {
        ContactsController.LOGGER.info("[createNewContacts][VerifyLogin Success]");
        Response<?> response = contactsService.create(aci, headers);
        if (response.getStatus() != 1)
            createNewContacts_error.increment();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @CrossOrigin(origins = "*")
    @PostMapping(path = "/contacts/admin")
    @Timed(value = "ts.contacts.createNewContactsAdmin")
    public HttpEntity<?> createNewContactsAdmin(@RequestBody Contacts aci, @RequestHeader HttpHeaders headers) {
        aci.setId(UUID.randomUUID().toString());
        ContactsController.LOGGER.info("[createNewContactsAdmin][Create Contacts In Admin]");
        Response<?> response = contactsService.createContacts(aci, headers);
        if (response.getStatus() != 1)
            createNewContactsAdmin_error.increment();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @CrossOrigin(origins = "*")
    @DeleteMapping(path = "/contacts/{contactsId}")
    @Timed(value = "ts.contacts.deleteContacts")
    public HttpEntity deleteContacts(@PathVariable String contactsId, @RequestHeader HttpHeaders headers) {
        Response<?> response = contactsService.delete(contactsId, headers);
        if (response.getStatus() != 1)
            deleteContacts_error.increment();
        return ok(response);
    }


    @CrossOrigin(origins = "*")
    @PutMapping(path = "/contacts")
    @Timed(value = "ts.contacts.modifyContacts")
    public HttpEntity modifyContacts(@RequestBody Contacts info, @RequestHeader HttpHeaders headers) {
        ContactsController.LOGGER.info("[Contacts modifyContacts][Modify Contacts] ContactsId: {}", info.getId());
        Response<?> response = contactsService.modify(info, headers);
        if (response.getStatus() != 1)
            modifyContacts_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/contacts/account/{accountId}")
    @Timed(value = "ts.contacts.findContactsByAccountId")
    public HttpEntity findContactsByAccountId(@PathVariable String accountId, @RequestHeader HttpHeaders headers) {
        ContactsController.LOGGER.info("[findContactsByAccountId][Find Contacts By Account Id][accountId: {}]", accountId);
        ContactsController.LOGGER.info("[ContactsService][VerifyLogin Success]");
        Response<?> response = contactsService.findContactsByAccountId(accountId, headers);
        if (response.getStatus() != 1)
            findContactsByAccountId_error.increment();
        return ok(response);
    }

    @CrossOrigin(origins = "*")
    @GetMapping(path = "/contacts/{id}")
    @Timed(value = "ts.contacts.getContactsByContactsId")
    public HttpEntity getContactsByContactsId(@PathVariable String id, @RequestHeader HttpHeaders headers) {
        ContactsController.LOGGER.info("[ContactsService][Contacts Id Print][id: {}]", id);
        ContactsController.LOGGER.info("[ContactsService][VerifyLogin Success]");
        Response<?> response = contactsService.findContactsById(id, headers);
        if (response.getStatus() != 1)
            getContactsByContactsId_error.increment();
        return ok(response);
    }
}

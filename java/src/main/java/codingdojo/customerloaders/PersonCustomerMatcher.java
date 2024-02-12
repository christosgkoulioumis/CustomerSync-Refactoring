package codingdojo.customerloaders;

import codingdojo.exception.ConflictException;
import codingdojo.model.Customer;
import codingdojo.model.CustomerMatches;
import codingdojo.model.CustomerType;
import codingdojo.model.ExternalCustomer;
import codingdojo.repository.CustomerDataAccess;

public class PersonCustomerMatcher {

    private static final String MATCH_TERM_EXTERNAL_ID = "ExternalId";
    private final CustomerDataAccess customerDataAccess;

    public PersonCustomerMatcher(CustomerDataAccess customerDataAccess) {
        this.customerDataAccess = customerDataAccess;
    }

    public CustomerMatches getPersonCustomerMatches(ExternalCustomer externalCustomer) {
        final String externalId = externalCustomer.getExternalId();

        CustomerMatches customerMatches = loadPersonCustomer(externalId);

        if (customerMatches.getCustomer() != null) {
            if (!MATCH_TERM_EXTERNAL_ID.equals(customerMatches.getMatchTerm())) {
                Customer customer = customerMatches.getCustomer();
                customer.setExternalId(externalId);
                customer.setMasterExternalId(externalId);
            }
        }

        return customerMatches;
    }

    private CustomerMatches loadPersonCustomer(String externalId) {
        CustomerMatches matches = new CustomerMatches();
        Customer matchByPersonalNumber = this.customerDataAccess.findCustomerByExternalId(externalId);
        if (matchByPersonalNumber != null) {
            validatePersonCustomerType(matchByPersonalNumber, externalId);
            matches.setCustomer(matchByPersonalNumber);
            matches.setMatchTerm(MATCH_TERM_EXTERNAL_ID);
        }
        return matches;
    }


    private static void validatePersonCustomerType(Customer matchByPersonalNumber, String externalId) {
        if (!CustomerType.PERSON.equals(matchByPersonalNumber.getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a person");
        }
    }

}

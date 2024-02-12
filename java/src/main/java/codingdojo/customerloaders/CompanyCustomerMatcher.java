package codingdojo.customerloaders;

import codingdojo.exception.ConflictException;
import codingdojo.model.Customer;
import codingdojo.model.CustomerMatches;
import codingdojo.model.CustomerType;
import codingdojo.model.ExternalCustomer;
import codingdojo.repository.CustomerDataAccess;

public class CompanyCustomerMatcher {

    private static final String MATCH_TERM_EXTERNAL_ID = "ExternalId";
    private static final String MATCH_TERM_COMPANY_NUMBER = "CompanyNumber";
    private final CustomerDataAccess customerDataAccess;

    public CompanyCustomerMatcher(CustomerDataAccess customerDataAccess) {
        this.customerDataAccess = customerDataAccess;
    }

    public CustomerMatches getCompanyCustomerMatches(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = loadCompanyMatches(externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());
        if (MATCH_TERM_EXTERNAL_ID.equals(customerMatches.getMatchTerm())) {
            return handleExternalIdMatches(customerMatches, externalCustomer.getCompanyNumber());
        } else if (MATCH_TERM_COMPANY_NUMBER.equals(customerMatches.getMatchTerm())) {
            return handleCompanyNumberMatches(customerMatches, externalCustomer.getExternalId());
        }

        return customerMatches;
    }

    private CustomerMatches loadCompanyMatches(String externalId, String companyNumber) {
        Customer matchByExternalId = this.customerDataAccess.findCustomerByExternalId(externalId);
        if (matchByExternalId != null) {
            return loadExternalIdMatches(matchByExternalId, externalId);
        } else {
            return loadCompanyNumberMatches(externalId, companyNumber);
        }
    }

    private CustomerMatches handleExternalIdMatches(CustomerMatches customerMatches, String externalCompanyNumber){
        String customerMatchesCompanyNumber = customerMatches.getCustomer().getCompanyNumber();
        if (!externalCompanyNumber.equals(customerMatchesCompanyNumber)) {
            customerMatches.getCustomer().setMasterExternalId(null);
            customerMatches.addDuplicate(customerMatches.getCustomer());
            customerMatches.setCustomer(null);
            customerMatches.setMatchTerm(null);
        }

        return customerMatches;
    }

    private CustomerMatches handleCompanyNumberMatches(CustomerMatches customerMatches, String externalId){
        Customer customer = customerMatches.getCustomer();
        customer.setExternalId(externalId);
        customer.setMasterExternalId(externalId);
        customerMatches.addDuplicate(null);

        return customerMatches;
    }

    private CustomerMatches loadExternalIdMatches(Customer matchByExternalId, String externalId) {
        CustomerMatches matches = new CustomerMatches();
        matches.setCustomer(matchByExternalId);
        matches.setMatchTerm(MATCH_TERM_EXTERNAL_ID);

        addMasterIdMatches(externalId, matches);

        validateCompanyCustomerType(matches, externalId);

        return matches;
    }

    private CustomerMatches loadCompanyNumberMatches(String externalId, String companyNumber) {
        CustomerMatches matches = new CustomerMatches();
        Customer matchByCompanyNumber = this.customerDataAccess.findCustomerByCompanyNumber(companyNumber);
        if (matchByCompanyNumber != null) {
            matches.setCustomer(matchByCompanyNumber);
            matches.setMatchTerm(MATCH_TERM_COMPANY_NUMBER);
            validateCompanyExternalId(matches.getCustomer().getExternalId(), externalId, companyNumber);
        }
        return matches;
    }

    private void addMasterIdMatches(String externalId, CustomerMatches matches) {
        Customer matchByMasterId = this.customerDataAccess.findCustomerByMasterExternalId(externalId);
        if (matchByMasterId != null) matches.addDuplicate(matchByMasterId);
    }

    private static void validateCompanyCustomerType(CustomerMatches companyCustomerMatches, String externalId) {
        if (companyCustomerMatches.getCustomer() != null && !CustomerType.COMPANY.equals(companyCustomerMatches.getCustomer().getCustomerType())) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        }
    }

    private static void validateCompanyExternalId(String customerExternalId, String externalId, String companyNumber) {
        if (customerExternalId != null && !externalId.equals(customerExternalId)) {
            throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId);
        }
    }

}

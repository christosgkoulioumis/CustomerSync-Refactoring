package codingdojo.customerloaders;

import codingdojo.model.CustomerMatches;
import codingdojo.model.ExternalCustomer;
import codingdojo.repository.CustomerDataAccess;

public class CustomerMatcher {

    private final CompanyCustomerMatcher companyCustomerMatcher;
    private final PersonCustomerMatcher personCustomerMatcher;

    public CustomerMatcher(CustomerDataAccess customerDataAccess) {
        this.companyCustomerMatcher = new CompanyCustomerMatcher(customerDataAccess);
        this.personCustomerMatcher = new PersonCustomerMatcher(customerDataAccess);
    }

    public CustomerMatches getCustomerMatches(ExternalCustomer externalCustomer) {
        if (externalCustomer.isCompany()) {
            return companyCustomerMatcher.getCompanyCustomerMatches(externalCustomer);
        } else {
            return personCustomerMatcher.getPersonCustomerMatches(externalCustomer);
        }
    }

}

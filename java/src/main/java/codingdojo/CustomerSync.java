package codingdojo;

import codingdojo.customerloaders.CustomerMatcher;
import codingdojo.model.*;
import codingdojo.repository.CustomerDataAccess;
import codingdojo.repository.CustomerDataLayer;

import java.util.List;

public class CustomerSync {

    private final CustomerMatcher customerMatcher;
    private final CustomerDataAccess customerDataAccess;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess customerDataAccess) {
        this.customerMatcher = new CustomerMatcher(customerDataAccess);
        this.customerDataAccess = customerDataAccess;
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = customerMatcher.getCustomerMatches(externalCustomer);

        Customer customer = customerMatches.getCustomer();

        boolean created = false;
        if (customer == null) {
            customer = newCustomer(externalCustomer);
            created = true;
        }

        updateFields(externalCustomer, customer);

        createOrUpdateRecord(customer);

        handleDuplicates(externalCustomer, customerMatches);

        updateRelations(externalCustomer, customer);

        return created;
    }

    private Customer newCustomer(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        return customer;
    }

    private void createOrUpdateRecord(Customer customer) {
        if (customer.getInternalId() == null) {
            createCustomer(customer);
        } else {
            updateCustomer(customer);
        }
    }


    private void handleDuplicates(ExternalCustomer externalCustomer, CustomerMatches customerMatches) {
        if (customerMatches.hasDuplicates()) {
            for (Customer duplicate : customerMatches.getDuplicates()) {
                updateDuplicate(externalCustomer, duplicate);
            }
        }
    }

    private void updateRelations(ExternalCustomer externalCustomer, Customer customer) {
        List<ShoppingList> consumerShoppingLists = externalCustomer.getShoppingLists();
        for (ShoppingList consumerShoppingList : consumerShoppingLists) {
            customer.addShoppingList(consumerShoppingList);
            updateCustomer(customer);
            this.customerDataAccess.updateShoppingList(consumerShoppingList);
        }
    }

    private Customer updateCustomer(Customer customer) {
        return this.customerDataAccess.updateCustomerRecord(customer);
    }

    private void updateDuplicate(ExternalCustomer externalCustomer, Customer duplicate) {
        if (duplicate == null) {
            duplicate = newCustomer(externalCustomer);
        }
        duplicate.setName(externalCustomer.getName());
        createOrUpdateRecord(duplicate);
    }

    private Customer createCustomer(Customer customer) {
        return this.customerDataAccess.createCustomerRecord(customer);
    }

    private void updateFields(ExternalCustomer externalCustomer, Customer customer) {
        customer.setName(externalCustomer.getName());
        customer.setAddress(externalCustomer.getPostalAddress());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        if (externalCustomer.isCompany()) {
            customer.setCustomerType(CustomerType.COMPANY);
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        } else {
            customer.setCustomerType(CustomerType.PERSON);
            if (!externalCustomer.getBonusPointsBalance().equals(customer.getBonusPointsBalance())) {
                customer.setBonusPointsBalance(externalCustomer.getBonusPointsBalance());
            }
        }
    }

}

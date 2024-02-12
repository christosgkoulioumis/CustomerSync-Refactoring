package codingdojo.repository;

import codingdojo.model.ShoppingList;
import codingdojo.model.Customer;

public class CustomerDataAccess {

    private final CustomerDataLayer customerDataLayer;

    public CustomerDataAccess(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public Customer findCustomerByExternalId(String externalId) {
        return this.customerDataLayer.findByExternalId(externalId);
    }

    public Customer findCustomerByMasterExternalId(String externalId) {
        return this.customerDataLayer.findByMasterExternalId(externalId);
    }

    public Customer findCustomerByCompanyNumber(String companyNumber) {
        return this.customerDataLayer.findByCompanyNumber(companyNumber);
    }

    public Customer updateCustomerRecord(Customer customer) {
        return customerDataLayer.updateCustomerRecord(customer);
    }

    public Customer createCustomerRecord(Customer customer) {
        return customerDataLayer.createCustomerRecord(customer);
    }

    public void updateShoppingList(ShoppingList consumerShoppingList) {
        customerDataLayer.updateShoppingList(consumerShoppingList);
    }
}

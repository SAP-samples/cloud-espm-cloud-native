package com.sap.refapps.espm.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

/**
 * This is the Customer entity class 
 * which defines the data model for customer.
 *
 */
@Entity
@Table(name = "ESPM_CUSTOMER")
public class Customer {

	@Id
	@Column(name = "CUSTOMER_ID", length = 10, unique = true)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "CustomerIDGenerator")
	@TableGenerator(name = "CustomerIDGenerator", table = "CUST_ID_GENERATOR", pkColumnName = "GENERATOR_NAME", 
	pkColumnValue = "Cust", valueColumnName = "GENERATOR_VALUE", initialValue = 1000000000, allocationSize = 100)
	private String customerId;

	@Column(name = "EMAIL_ADDRESS", unique = true, nullable = false)
	private String emailAddress;

	@Column(name = "PHONE_NUMBER", length = 30)
	private String phoneNumber;

	@Column(name = "FIRST_NAME", length = 40)
	private String firstName;

	@Column(name = "LAST_NAME", length = 40)
	private String lastName;

	@Column(name = "DATE_OF_BIRTH", nullable = false)
	private String dateOfBirth;

	@Column(name = "CITY", length = 40)
	private String city;

	@Column(name = "POSTAL_CODE", length = 10)
	private String postalCode;

	@Column(name = "STREET", length = 60)
	private String street;

	@Column(name = "HOUSE_NUMBER", length = 10)
	private String houseNumber;

	@Column(name = "COUNTRY", length = 3)
	private String country;

	public String getCustomerId() {
		return customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getHouseNumber() {
		return houseNumber;
	}

	public void setHouseNumber(String houseNumber) {
		this.houseNumber = houseNumber;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

}

package com.BRS.BookRecomendation.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.BRS.BookRecomendation.Entities.Address;
import com.BRS.BookRecomendation.repository.AddressRepository;

@Service
public class AddressService {

    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);

    @Autowired
    private AddressRepository addressRepository;

    // Add or Update Address
    public Address saveOrUpdateAddress(Long userId, Address address) {
        logger.info("Attempting to save or update address for user: {}", userId);
        Address existingAddress = addressRepository.findById(userId).orElse(null);

        if (existingAddress != null) {
            // Update existing address
            logger.debug("Updating existing address for user: {}", userId);
            existingAddress.setStreet(address.getStreet());
            existingAddress.setCity(address.getCity());
            existingAddress.setState(address.getState());
            existingAddress.setCountry(address.getCountry());
            existingAddress.setZipCode(address.getZipCode());
            existingAddress.setPhoneNumber(address.getPhoneNumber());
            Address savedAddress = addressRepository.save(existingAddress);
            logger.info("Successfully updated address for user: {}", userId);
            return savedAddress;
        } else {
            // Create new address
            logger.debug("Creating new address for user: {}", userId);

            Address savedAddress = addressRepository.save(address);
            logger.info("Successfully created new address for user: {}", userId);
            return savedAddress;
        }
    }

    // Get User's Address
    public Optional<Address> getUserAddress(Long userId) {
        logger.info("Retrieving address for user: {}", userId);
        Optional<Address> address = addressRepository.findById(userId);
        if (address.isPresent()) {
            logger.debug("Address found for user: {}", userId);
        } else {
            logger.debug("No address found for user: {}", userId);
        }
        return address;
    }

    public void deleteAddress(Long userId) {
        addressRepository.deleteById(userId);
    }

}
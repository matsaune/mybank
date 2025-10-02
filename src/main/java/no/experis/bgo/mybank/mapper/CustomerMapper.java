package no.experis.bgo.mybank.mapper;

import no.experis.bgo.mybank.api.model.Customer;
import no.experis.bgo.mybank.api.model.CustomerRequest;
import no.experis.bgo.mybank.model.CustomerEntity;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Customer (API layer) and CustomerEntity (domain layer).
 */
public class CustomerMapper {

    /**
     * Converts a domain entity to an API model.
     *
     * @param entity the customer entity
     * @return the customer API model
     */
    public static Customer toApiModel(CustomerEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Customer()
                .id(entity.id())
                .firstName(entity.firstName())
                .lastName(entity.lastName())
                .email(entity.email())
                .personalIdNumber(entity.personalIdNumber())
                .phoneNumber(entity.phoneNumber())
                .created(toOffsetDateTime(entity.created()))
                .updated(toOffsetDateTime(entity.updated()));
    }

    private static OffsetDateTime toOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }

    /**
     * Converts an API model to a domain entity.
     *
     * @param request the customer API request
     * @return the customer entity
     */
    public static CustomerEntity toEntity(CustomerRequest request) {
        if (request == null) {
            return null;
        }

        return new CustomerEntity()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .personalIdNumber(request.getPersonalIdNumber())
                .phoneNumber(request.getPhoneNumber());
    }

    /**
     * Updates an existing domain entity with data from an API request.
     *
     * @param entity the existing customer entity to update
     * @param request the customer API request containing new data
     * @return the updated entity
     */
    public static CustomerEntity updateEntity(CustomerEntity entity, CustomerRequest request) {
        if (entity == null || request == null) {
            return entity;
        }

        return entity
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .personalIdNumber(request.getPersonalIdNumber())
                .phoneNumber(request.getPhoneNumber());
    }

    /**
     * Converts a list of domain entities to a list of API models.
     *
     * @param entities the list of customer entities
     * @return the list of customer API models
     */
    public static List<Customer> toApiModelList(List<CustomerEntity> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .map(CustomerMapper::toApiModel)
                .collect(Collectors.toList());
    }
}

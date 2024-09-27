package uk.gov.service.notify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

class ValidatingJsonMapper {

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final boolean skipValidation;

    ValidatingJsonMapper(NotificationClientOptions clientOptions) {
        this.skipValidation = Boolean.parseBoolean(NotifyUtils.getProperty(clientOptions, NotificationClientOptions.Options.VALIDATION_SKIP.getPropertyKey()));
        if(this.skipValidation) {
            this.validator = null;
        } else {
            try (ValidatorFactory validatorFactory = Validation.byDefaultProvider()
                    .configure()
                    .messageInterpolator(new ParameterMessageInterpolator())
                    .buildValidatorFactory()) {
                this.validator = validatorFactory.getValidator();
            }
        }
        boolean failOnUnknownValues = Boolean.parseBoolean(NotifyUtils.getProperty(clientOptions, NotificationClientOptions.Options.FAIL_ON_UNKNOWN_VALUES.getPropertyKey()));;
        this.objectMapper = new ObjectMapper()
                // the following two lines are needed to (de)serialize ZonedDateTime in ISO8601 format
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                // this allows the client to ignore unknown values sent in API responses
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failOnUnknownValues);
    }

    <ValueType> String writeValueAsString(ValueType value) throws JsonProcessingException, NotificationClientException {
        if(!skipValidation) {
            validate(value);
        }
        return objectMapper.writeValueAsString(value);
    }

    <ValueType> ValueType readValue(InputStream inputStream, Class<ValueType> clazz) throws IOException, NotificationClientException {
        final ValueType value = objectMapper.readValue(inputStream, clazz);
        if(!skipValidation) {
            validate(value);
        }
        return value;
    }

    private <ValueType> void validate(ValueType value) throws NotificationClientException {
        Set<ConstraintViolation<ValueType>> constraintViolations = validator.validate(value);
        if (!constraintViolations.isEmpty()) {
            final String message = constraintViolations.stream()
                    .map(v -> v.getRootBeanClass().getName() + "." + v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            throw new NotificationClientException(message);
        }
    }
}
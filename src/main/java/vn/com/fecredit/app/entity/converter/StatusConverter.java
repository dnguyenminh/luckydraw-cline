package vn.com.fecredit.app.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.com.fecredit.app.enums.EntityStatus;

@Converter
public class StatusConverter implements AttributeConverter<EntityStatus, String> {

    @Override
    public String convertToDatabaseColumn(EntityStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Override
    public EntityStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            return EntityStatus.valueOf(dbData);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

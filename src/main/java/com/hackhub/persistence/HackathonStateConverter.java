package com.hackhub.persistence;

import com.hackhub.domain.hackathon.HackathonStatus;
import com.hackhub.domain.state.HackathonState;
import com.hackhub.domain.state.HackathonStateFactory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class HackathonStateConverter implements AttributeConverter<HackathonState, String> {

    @Override
    public String convertToDatabaseColumn(HackathonState attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getStatus().name();
    }

    @Override
    public HackathonState convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return HackathonStateFactory.fromStatus(HackathonStatus.valueOf(dbData));
    }
}

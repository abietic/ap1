package com.abietic.ap1.serializer;

import java.io.IOException;

import org.joda.time.DateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JodaDateTimeJsonSerializer extends JsonSerializer<DateTime>{

    @Override
    public void serialize(DateTime dateTime, JsonGenerator jsonGenerator, SerializerProvider arg2) throws IOException {
        jsonGenerator.writeString(dateTime.toString("yyyy-MM-dd HH:mm:ss"));
    }
    
}

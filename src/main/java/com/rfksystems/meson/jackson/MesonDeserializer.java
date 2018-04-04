package com.rfksystems.meson.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rfksystems.meson.Meson;

import java.io.IOException;

public class MesonDeserializer extends StdDeserializer<Meson> {
    private static final long serialVersionUID = 7313427364359684471L;

    public MesonDeserializer() {
        super(Meson.class);
    }

    @Override
    public Meson deserialize(
            final JsonParser parser,
            final DeserializationContext context
    ) throws IOException {
        final String value = parser.getValueAsString();

        if (null == value || value.isEmpty()) {
            return null;
        }

        return new Meson(value);
    }

    @Override
    public Object getEmptyValue(DeserializationContext context) throws JsonMappingException {
        return null;
    }
}

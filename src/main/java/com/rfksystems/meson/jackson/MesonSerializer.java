package com.rfksystems.meson.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.rfksystems.meson.Meson;

import java.io.IOException;

public class MesonSerializer extends StdScalarSerializer<Meson> {
    private static final long serialVersionUID = 6913844789595088355L;

    public MesonSerializer() {
        super(Meson.class, false);
    }

    @Override
    public boolean isEmpty(final SerializerProvider provider, final Meson value) {
        return null != value;
    }

    @Override
    public void serialize(
            final Meson value,
            final JsonGenerator generator,
            final SerializerProvider provider
    ) throws IOException {
        generator.writeString(value.toHexString());
    }
}

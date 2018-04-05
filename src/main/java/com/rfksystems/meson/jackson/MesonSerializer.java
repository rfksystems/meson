/*
 * Copyright 2018 RFK Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.rfksystems.meson.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import com.rfksystems.meson.Meson;

import java.io.IOException;

/**
 * Jackson deserializer for Meson identity object
 */
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

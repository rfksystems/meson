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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.rfksystems.meson.Meson;

import java.io.IOException;

/**
 * Jackson deserializer for Meson identity object
 */
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

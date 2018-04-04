package com.rfksystems.meson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class JacksonTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void maps_to_json_and_back() throws Exception {
        final Meson meson = new Meson("01629128e71ed9686a4716e2f55e");
        final String json = objectMapper.writeValueAsString(meson);
        assertThat(json).isEqualTo("\"01629128e71ed9686a4716e2f55e\"");

        final Meson mesonFromJson = objectMapper.readValue("\"01629128e71ed9686a4716e2f55e\"", Meson.class);

        assertThat(meson).isEqualTo(mesonFromJson);
        assertThat(meson.toHexString()).isEqualTo(mesonFromJson.toHexString());
    }

    @Test
    public void maps_in_pojo_and_back() throws Exception {
        final Meson meson = new Meson("01629128e71ed9686a4716e2f55e");
        final MojoPojo mojoPojo = new MojoPojo(meson);

        assertThat(mojoPojo.getMeson()).isSameAs(meson);

        final String mojoPojoString = objectMapper.writeValueAsString(mojoPojo);

        assertThat(mojoPojoString).isEqualTo("{\"meson\":\"01629128e71ed9686a4716e2f55e\"}");

        final MojoPojo mojoPojoFromJson = objectMapper.readValue(
                "{\"meson\":\"01629128e71ed9686a4716e2f55e\"}",
                MojoPojo.class
        );

        assertThat(mojoPojo.getMeson()).isEqualTo(mojoPojoFromJson.getMeson());
        assertThat(mojoPojoFromJson.getMeson().toHexString()).isEqualTo("01629128e71ed9686a4716e2f55e");
    }

    private static final class MojoPojo {
        @JsonProperty
        private final Meson meson;

        @JsonCreator
        public MojoPojo(@JsonProperty("meson") final Meson meson) {
            this.meson = meson;
        }

        public Meson getMeson() {
            return meson;
        }
    }
}

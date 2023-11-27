package dev.mikita.issueservice.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.locationtech.jts.geom.Point;
import java.io.IOException;

/**
 * The type Point serializer.
 */
public class PointSerializer extends JsonSerializer<Point> {
    @Override
    public void serialize(Point value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("latitude", value.getY());
        gen.writeNumberField("longitude", value.getX());
        gen.writeEndObject();
    }
}
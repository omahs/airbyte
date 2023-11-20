/*
 * Copyright (c) 2023 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.cdk.integrations.destination.s3.avro;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.airbyte.cdk.integrations.base.JavaBaseConstants;
import io.airbyte.cdk.protocol.PartialAirbyteRecordMessage;
import io.airbyte.commons.jackson.MoreMappers;
import io.airbyte.commons.json.Jsons;
import java.util.UUID;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import tech.allegro.schema.json2avro.converter.JsonAvroConverter;

public class AvroRecordFactory {

  private static final ObjectMapper MAPPER = MoreMappers.initMapper();
  private static final ObjectWriter WRITER = MAPPER.writer();

  private final Schema schema;
  private final JsonAvroConverter converter;

  public AvroRecordFactory(final Schema schema, final JsonAvroConverter converter) {
    this.schema = schema;
    this.converter = converter;
  }

  public GenericData.Record getAvroRecord(final UUID id, final PartialAirbyteRecordMessage recordMessage) throws JsonProcessingException {
    final ObjectNode jsonRecord = MAPPER.createObjectNode();
    jsonRecord.put(JavaBaseConstants.COLUMN_NAME_AB_ID, id.toString());
    jsonRecord.put(JavaBaseConstants.COLUMN_NAME_EMITTED_AT, recordMessage.getEmittedAt());
    // Explicitly deserialize the json. We need the full json structure to convert it to avro.
    jsonRecord.setAll((ObjectNode) Jsons.deserializeExact(recordMessage.getSerializedData()));

    return converter.convertToGenericDataRecord(WRITER.writeValueAsBytes(jsonRecord), schema);
  }

  public GenericData.Record getAvroRecord(final JsonNode formattedData) throws JsonProcessingException {
    final var bytes = WRITER.writeValueAsBytes(formattedData);
    return converter.convertToGenericDataRecord(bytes, schema);
  }

}

package io.airbyte.cdk.integrations.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.airbyte.commons.json.Jsons;
import io.airbyte.validation.json.JsonSchemaValidator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public abstract class AirbyteConfig<CONFIG_TYPE extends AirbyteConfig> {

  public static abstract class AirbyteConfigBuilder<T2 extends AirbyteConfig> {
    protected final ObjectNode jsonConfig;
    public AirbyteConfigBuilder(AirbyteConfig<T2> airbyteConfig) {
      jsonConfig = (ObjectNode)Jsons.clone(airbyteConfig.jsonConfig);
    }

    public abstract T2 build();

    public void replaceNestedString(List<String> keys, String replacement) {
      Jsons.replaceNestedString(jsonConfig, keys, replacement);
    }

    public void replaceNestedInt(List<String> keys, int replacement) {
      Jsons.replaceNestedInt(jsonConfig, keys, replacement);
    }
  }
  private static final ConfigField<AirbyteConfig, Map<String, String>> JDBC_URL_PARAMS_FIELD =
      new StringToStringMapConfigField<>("jdbc_url_params");
  private static final ConfigField<AirbyteConfig, Map<String, String>> CONNECTION_PROPERTIES_FIELD =
      new StringToStringMapConfigField<>("connection_properties");

  protected final JsonNode jsonConfig;

  protected AirbyteConfig(JsonNode jsonConfig) {
    this.jsonConfig = jsonConfig;
  }
  public abstract AirbyteConfigBuilder<CONFIG_TYPE> cloneBuilder();


  public final String getStringOrNull(String... keys) {
    return Jsons.getStringOrNull(jsonConfig, keys);
  }

  public final String getStringOrNull(List<String> keys) {
    return Jsons.getStringOrNull(jsonConfig, keys);
  }

  public final int getIntOrZero(String... keys) {
    return Jsons.getIntOrZero(jsonConfig, keys);
  }

  public final int getIntOrZero(List<String> keys) {
    return Jsons.getIntOrZero(jsonConfig, keys);
  }

  public final Optional<JsonNode> getOptional(String... keys) {
    return Jsons.getOptional(jsonConfig, keys);
  }

  public final Set<String> validateWith(JsonSchemaValidator validator, JsonNode schemaJson) {
    return validator.validate(schemaJson, this.jsonConfig);
  }

  // HWe want to stay compatible with the json serialization, so this has to send a json string
  @Override
  public String toString() {
    return jsonConfig.toString();
  }

  final boolean has(ConfigField<AirbyteConfig, ?> key) {
    return jsonConfig.has(key.fieldName);
  }
  final JsonNode get(ConfigField<AirbyteConfig, ?> key) {
    return jsonConfig.get(key.fieldName);
  }

  public Map<String, String> getJdbcUrlParams() {
    return JDBC_URL_PARAMS_FIELD.convert(this);
  }

  public Map<String, String> getConnectionProperties() {
    return CONNECTION_PROPERTIES_FIELD.convert(this);
  }
}

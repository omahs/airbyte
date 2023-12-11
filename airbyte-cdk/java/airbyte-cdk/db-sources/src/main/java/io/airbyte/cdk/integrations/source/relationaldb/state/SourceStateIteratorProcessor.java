package io.airbyte.cdk.integrations.source.relationaldb.state;

import io.airbyte.protocol.models.v0.AirbyteMessage;
import io.airbyte.protocol.models.v0.AirbyteMessage.Type;
import io.airbyte.protocol.models.v0.AirbyteStateMessage;
import java.time.Instant;

public interface SourceStateIteratorProcessor {
  /**
   * Returns a state message that should be emitted at checkpoint.
   */
  AirbyteStateMessage generateStateMessageAtCheckpoint();

  /**
   * For the incoming record message, this method defines how the connector will consume it.
   */
  void processRecordMessage(final AirbyteMessage message);

  /**
   * At the end of the iteration, this method will be called and it will generate the final state message.
   * @return
   */
  AirbyteStateMessage createFinalStateMessage();

  /**
   * Determines if the iterator has reached checkpoint or not,
   * based on the time and number of record messages it has been processed since the last checkpoint.
   */
  boolean shouldEmitStateMessage(final long recordCount, final Instant lastCheckpoint);
}

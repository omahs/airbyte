# "Schemaless" Sources and Destinations

For every connector, Airbyte requires a catalog, which includes a data schema describing the shape of data being emitted by the source.
This schema will be used by the destination so that it's prepared to populate the data during the sync.

## What is a Schemaless Source?

Schemaless sources are sources for which there is no requirement or expectation that records will conform to a particular pattern.
For example, in a MongoDB database, there's no requirement that the keys in one document are the same as the keys in the next.
Similarly, if you're syncing data from a file-based source, the files that are present in your source may not all have the same schema.

Although the sources themselves may not conform to an obvious schema, Airbyte still needs to know the shape of the data in order to prepare the destination for the records.
For these sources, during the `discover` call, Airbyte offers 2 options for schema inference:

1. Dynamic schema inference.
2. A hardcoded "schemaless" schema.

### Dynamic schema inference
If this option is selected, Airbyte will compute the schema dynamically based on the contents of the source.
For example, for file-based sources, we look at up to 10 files and infer the schema based on the contents of those files.
For MongoDB, we infer the schema from a random sample of documents.
In both cases, as the contents of the source change, the schema can change too.

There are a few drawbacks to be aware of:
- If your data set is very large, the `discover` process can be very time consuming. 
- Because we may not use 100% of the available data to create the schema, your schema may be incomplete. Airbyte only syncs fields that are in the schema, so you may end up with incomplete data in the destination. We try to let you avoid this by configuring the number of files used for schema inference, but cannot guarantee that all fields will be present.

If you select dynamic schema inference and are syncing a file-based source, you will have the option to select a "schema validation" policy that gives you the ability to decide how the sync should behave if a record is encountered that does not conform to the schema.
1. Continue the sync. If you choose this option, Airbyte will sync the parts of each record that it is able to sync, but ignore any fields that aren't part of the schema.
2. Stop the sync and wait for rediscovery. If you choose this option, Airbyte will stop the in-progress sync. Before the next sync, Airbyte will re-infer the schema via a new `discover` call. This ensures that all fields will ultimately be synced. **Note: if your data changes frequently this could end up in an infinite loop.**

If your data set is very large or you anticipate that it will change often, we recommend using the "schemaless" schema.

### Schemaless schema
If this option is selected, the schema will always be `{"data": object}`, regardless of the contents of the data. The data will then be "wrapped" in a `"data"` key, and will arrive at the destination in a single column nested under this key.
This avoids a time-consuming or inaccurate `discover` phase and guarantees that everything shows up in your destination, at the expense of Airbyte being able to structure the data into different columns.

## Coming Soon: Unwrapping the data at Schemaless Destinations
In addition to not enforcing schema as sources, MongoDB and file storage systems don't require a schema at the destination. For this reason, if you are syncing data from a schemaless source to a schemaless destination and chose the "schemaless" schema option, Airbyte will offer the ability to "unwrap" the data at the destination so that it is not nested under a "data" key.

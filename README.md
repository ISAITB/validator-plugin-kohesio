# CSV validator for the Kohesio validator

This is a validator plugin for the Kohesio validator.

The entry point for the validation in the `PluginInterface` class, and specifically its `validate` method. The report 
returned from this method will be merged with the core validator's report. All rules are defined in the `eu.europa.ec.itb.kohesio.rules`
package.

To include this plugin in the Kohesio validator the following steps are needed:
   1. Build the validator using `mvn clean package`
   2. Copy `target/validator-plugin-kohesio-jar-with-dependencies.jar` to the resource folder of the core validator.
   3. Adapt the core validator's configuration to include the plugin in the validation.
   
Regarding step 3, and assuming the JAR is copied to folder `plugins` next to the configuration file, use the plugin for all validation types by including the following: 
```
validator.defaultPlugins.0.jar   = plugins/validator-plugin-kohesio-jar-with-dependencies.jar
validator.defaultPlugins.0.class = eu.europa.ec.itb.kohesio.PluginInterface
```

Alternatively, to use the plugin only for a specific validation type (e.g. `core`) define the following:
```
validator.plugins.core.0.jar   = plugins/validator-plugin-kohesio-jar-with-dependencies.jar
validator.plugins.core.0.class = eu.europa.ec.itb.kohesio.PluginInterface
```
See https://www.itb.ec.europa.eu/docs/guides/latest/validatingCSV/index.html#validator-configuration-properties for further details.

**Note:** This repository is monitored by the Test Bed's automation processes. Any changes pushed to its master branch will trigger
automaticaly an update of the Kohesio validator.
# Localization Support Plugin

Supporting infrastructure for standalone localization plugins.
This plugin doesn't provide notable end user features, and will be installed as dependency of other plugins that use the features provided by this plugin.

## Localization Overview

Jenkins supports multiple ways to provide localized resources, and each of them has a corresponding code path in this plugin.

### Localizer

The `localizer` library looks up `Messages_??.properties` files in packages for the generated `Messages` classes.
Support for localizing messages from the localizer library is added via the `io.jenkins.plugins.localization.support.localizer` package in this plugin.

### Stapler views

Stapler views look up `viewname_??.properties` files in directories with the same name as the view.
`io.jenkins.plugins.localization.support.stapler.ResourceBundleFactoryImpl` is a `ResourceBundleFactory` that gets installed by `StaplerManager`.
I ends up calling `LocalizationContributor#findResource(String, Class)` with a cleaned up `String` argument.

<!-- TODO details -->
<!-- TODO are localized views (the entire Jelly) a thing, or just resources? -->

### Descriptor#doHelp

#### Stapler views

`Descriptor#doHelp` will serve corresponding `help.jelly` views, if they exist, and lets Stapler localize them (see above).

#### HTML files

`Descriptor#doHelp` looks up `com/acme/package/MyDescribable/help-fieldname_??.html` HTML files and serves them at `/descriptor/myDescriptorSymbol/help/fieldname`, typically corresponding to specific fields in views.
It also supports `com/acme/package/MyDescribable/help_??.html` at `/descriptor/myDescriptorSymbol/help`.

Localization support of these is accomplished through setting `MetaClassLoader#debugLoader` to a classloader that has localization plugins on its class path.

<!-- TODO introduce a proper API for this into Stapler -->

### Core webapp resources

Jenkins core exposes `war/src/main/webapp/` resource files directly packaged into the war file via `Stapler#service` invoking `#openResourcePathByLocale`.
This in turn ends up invoking `LocaleDrivenResourceProvider#lookupResource`, an SPI implemented in Jenkins core as `MetaLocaleDrivenResourceProvider` from 2.173.
This in turn looks up implementations of `PluginLocaleDrivenResourceProvider` in plugins.
There is one, `PluginLocaleDrivenResourceProviderImpl` in `localization-support`.

### Plugin webapp resources

Plugins expose `src/main/webapp/` resource files directly packaged into the jpi file via `Plugin#doDynamic` at `/plugin/namehere/` invoking `StaplerResponse#serveLocalizedFile`.
This calls `#selectResourceByLocale` which ends up invoking `LocaleDrivenResourceProvider#lookupResource`.
See the previous section for further details.

## Administrative Monitor

The only user feature exposed by this plugin is an administrative monitor that complains when an incompatibility is detected.
Some of the mechanisms used to provide localized resources from implementing plugins are exclusive, and other plugins interfering with that would prevent this plugin from working.
If such a case is discovered, the administrative monitor informs users about the problem.

## Expected Layout

Implementing plugins need to provide the localized resources in the same general directory layout as they would be placed in core and individual plugins.

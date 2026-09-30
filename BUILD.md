# Build Requirements

* Java 1.9 or newer
* Gradle 8.x or newer to build
* &dagger; [Legacy Burp Extender API](https://portswigger.net/burp/extender/api/) v2.3; uses proprietary license
* &dagger; [MetaData Extractor](https://github.com/drewnoakes/metadata-extractor) v2.21.0; uses Apache License v2.0

&dagger; These will be auto-fetched if you build with Gradle.

The Burp plug-in is built with `./gradlew jar` (or be lazy and type `make`). After building, the plug-in can manually be loaded into Burp.  

To build for ZAP, it is easiest start by forking [ZAP Extensions](https://github.com/zaproxy/zap-extensions) or [my outdated repo](https://github.com/veggiespam/zap-extensions).  Then, overwrite your repo's ILS.java with the updated version.  Compile with `./gradlew :addOns:imagelocationscanner:build` and install *imagelocationscanner-{id}.zap* add-on file into ZAP via File &rarr; "Load Add-On File".


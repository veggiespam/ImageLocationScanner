# Here be dragons.  Best to just ignore this.

BURPJAR=image_location_scanner.jar
JARPATH=build/libs/image-location-scanner-all.jar

# If we let eclipse build for me.  Probably a better way.
BIN=$(HOME)/proj/eclipse-workspace/burp_image_scan/bin

$(JARPATH): src/com/veggiespam/imagelocationscanner/ILS.java
	gradle fatJar

clean:
	rm -rf $(BURPJAR)

distclean:
	rm -rf build

run:
	java -classpath $(JARPATH) com.veggiespam.imagelocationscanner.ILS

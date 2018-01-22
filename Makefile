# Here be dragons.  Best to just ignore this.

FATJAR=image-location-scanner-all.jar
JARPATH=build/libs/$(FATJAR)

# If we let eclipse build for me.  Probably a better way.
BIN=$(HOME)/proj/eclipse-workspace/burp_image_scan/bin

$(JARPATH): src/com/veggiespam/imagelocationscanner/ILS.java
	gradle fatJar

run: $(JARPATH)
	java -classpath $(JARPATH) com.veggiespam.imagelocationscanner.ILS ../*.jpg
	java -classpath $(JARPATH) com.veggiespam.imagelocationscanner.ILS -h ../*.jpg

clean:
	rm -f $(JARPATH)

distclean:
	rm -rf build

gradleclean: distclean
	rm -rf $(HOME)/.gradle/caches

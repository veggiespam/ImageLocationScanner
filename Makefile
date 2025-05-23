# Here be dragons.  Best to just ignore this.

FATJAR=image-location-scanner-all.jar
JARPATH=build/libs/$(FATJAR)

# as of now, Burp supports only up to v22 of Java, this is the latest brew LTS version.
JAVA_HOME=/opt/homebrew/opt/openjdk@21

GRADLE_OPTS=--warning-mode all

$(JARPATH): src/com/veggiespam/imagelocationscanner/ILS.java src/burp/BurpExtender.java
	gradle   $(GRADLE_OPTS)  jar

run: $(JARPATH)
	java -classpath $(JARPATH) com.veggiespam.imagelocationscanner.ILS
	java -classpath $(JARPATH) com.veggiespam.imagelocationscanner.ILS ../*.jpg
	java -classpath $(JARPATH) com.veggiespam.imagelocationscanner.ILS -h ../*.jpg

clean:
	rm -f $(JARPATH)

distclean:
	rm -rf build

gradleclean: distclean
	rm -rf $(HOME)/.gradle/caches

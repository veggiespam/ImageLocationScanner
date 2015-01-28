
# I let eclipse build for me.  Probably a better way.

BIN=$(HOME)/proj/eclipse-workspace/burp_image_scan/bin
BURPJAR=imagelocationscanner_burp.jar


$(BURPJAR): 
	mkdir -p burp-build
	cd burp-build ; unzip -q ../lib/sanselan-0.97-incubator.jar ; cd ..
	cp -r $(BIN)/* burp-build
	cp BappManifest.bmf burp-build
	cp LICENSE burp-build
	pandoc README.md | sed 's/&quot;/"/g; s/<ol style="list-style-type: decimal">/<ol>/g;' > burp-build/BappDescription.html
	cd burp-build ; zip -q -r ../$(BURPJAR) META-INF org burp com ; cd ..

x: 
	rm -rf dest
	mkdir dest
	cp -R $(BIN)/com $(BIN)/burp dest
	cd dest ; unzip ../sanselan-0.97-incubator.jar ; cd ..
	touch x

compile:
	

clean:
	rm -rf $(BURPJAR) burp-build

run:
	java -classpath $(BURPJAR) com.veggiespam.imagelocationscanner.ILS

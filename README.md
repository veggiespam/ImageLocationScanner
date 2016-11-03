# Image Location Scanner

Passively scans for GPS location and other privacy-related exposures in images during normal
security assessments of websites via plug-ins for both Burp & ZAP.  Image
Location Scanner assists in situations where end users may post profile
images and possibly give away their home location, e.g. a dating site or
children's chatroom.

More information on this topic, including a whitepaper 
based on a real-world site audit given as a presentation at the New
Jersey chapter of the OWASP organization, can be found at
[www.veggiespam.com/ils/](http://www.veggiespam.com/ils/) .

This software finds the GPS information inside of Exif tags, IPTC codes,
and proprietary Panasonic/Lumix codes. Then, the Image Location Scanner flags the
findings in the
Burp Scanner or ZAP Alerts list as an information message.  It would be
up to the auditor to determine if location exposure is truly a security
risk based on context. 

## Contributors

Special thanks to:

* The fine folks at [Aspect Security](https://www.aspectsecurity.com/) for
  performing initial tests of the alpha software and providing awesome
  feedback.
* Simon Bennetts, the leader of the ZAP team, for code reviews and
  help adding to the alpha channel.

# Usage Requirements
The Image Location Scanner runs as both a Burp and ZAP plug-in.
Requires:

* Burp Pro, 1.4 or newer
  [http://portswigger.net/burp/](ProtSwigger Burp web site)
* ZAP, 2.4.x or newer
  [https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project](OWASP
  ZAP web site)

## Burp Installation

Burp Application Store: Launch Burp and click Extender tab &rarr;
Bapp Store &rarr; left pane &rarr; Image Location Scanner.  In the right window pane, the
version and description of the plug-in will be shown; click the Install
button to download and activate.

Manual Install: Go to Extender &rarr; Extensions &rarr; Add.  Choose the
type as Java, choose the Image Location Scanner jar file (you built or
downloaded), leave Standard Output & Error as "Show in UI" and then
click Next.  The next screen will show the "Image Location Scanner:
plug-in version 0.3" if successful or display errors on the Error tab.
Click close to return to Burp.

Note: This is a scanner-type plug-in and the scanner is disabled in Burp
Free version.  So, the plug-in will only function inside of Burp Pro.

## ZAP Installation

The Image Location Scanner is available as part of the alpha channel
passive scanners
in the ZAP Marketplace.  It also can be downloaded and compiled directly
into ZAP.  At this time, v0.2 is in the alpha channel and v0.3 will be
available in the future.  ZAP code is also mirrored in the ZAP source tree.

## Sample Run

Configure the web browser to proxy through Burp or ZAP per the
instructions of those products.  Then, browse to a few sample sites to
see Alerts being raised:

* Sample Exif Site: [ReadExifData.com](http://readexifdata.com/)
* MetaData Extractor has tons of examples:
[MDE examples](https://github.com/drewnoakes/metadata-extractor-images/tree/master/jpg).
To view the "raw" URLs below, you will need to first go to the GitHub
URL above to get a session cookie:
  - https://github.com/drewnoakes/metadata-extractor/wiki/SampleOutput
  - [iPhone 4](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Apple%20iPhone%204.jpg)
  - [FujiFilm FinePix S1 Pro](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/FujiFilm%20FinePixS1Pro%20(1).jpg)
  - [IPTC data](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Issue%20122.jpg)
  - [Canon Powershot A2500](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Canon%20Powershot%20A2500.JPG)
  - [Canon EOS Rebel T3i](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Canon%20EOS%20REBEL%20T3i.jpg)
  - Proprietary Panasonic tags with embedded location example: [MDE Panasonic example](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Panasonic%20DMC-TZ10.jpg)
* This professional photographer leaves Exif in many photos: [Raia.com](http://raia.com/)

The ILS jar file contains a `main()` function,  so it is possible to
directly run the scanner from the command line on local files.  The
classpath must contain the ILS jar file along with the supporting jars
for the MetaData Extractor and the Adobe XMP library.  To from the
command line, just do:

```
$ java ...
Java Image Location Scanner
Usage: java ILS.class [-h|-t] file1.jpg file2.png file3.txt [...]
	-h : optional specifer to output results in HTML format
	-t : optional specifer to output results in plain text format

# Basic call with classpath
java -classpath ILS.jar:xmp.jar:mde.jar   com.veggiespam.imagelocationscanner.ILS  file1.jpg file2.png file3.tiff

# Call using the jar file from the Burp packaging
java -classpath image_location_scanner.jar com.veggiespam.imagelocationscanner.ILS  file1.jpg file2.png file3.tiff

# Command line output
Processing ../Panasonic DMC-TZ10.jpg : Location Exif_GPS: 53° 8' 49.65", 8° 10' 45.1" 
Location Panasonic: City = OLDENBURG (OLDB.)
Location Panasonic: Country = GERMANY
Location Panasonic: State = OLDENBURG (OLDB.)
Privacy Panasonic: Internal Serial Number = F541005110191

Processing ../Panasonic Lumix DMC-LX7.jpg : Privacy Panasonic: Internal Serial Number = F111311090158

Processing ../j2.jpg : Location Exif_GPS: 40° 18' 54.92", -74° 39' 37.85" 
```

Note the names of the jar files could be different, please confirm them.

# FAQ

* Why do I see two sets of Exif_GPS coordinates  (or other tag)
	- This means the image has been embedded with multiple Exif tags of
	the same type.  Thus more than one GPS location can appear.  The ILS
	software displays all that are detected.
* You missed the serial number for Camera Type X
	- Could be true.  This information exposure list was built by
	scanning all tags availbable as part of MDE.  If something new was
	added, then ILS needs to also account for it.  File a bug report
	on github.
* Why does it say "City = " with no city listed
	- It actually says "City = \\0\\0\\0\\0\\0 ..." with maybe 64 nulls.
	In newer versions ILS, we simply filter out strings that start with
	a null character.  We assume someone isn't hiding data there.
* When I use ZAP, nothing shows up
	- If you have images disabled in Global Exclude URL, then the
	passive image scanner, like ILS, will be unable to see the images
	and report on privacy issues.


# Build Requirements

* Java 1.6 or newer
* Eclipse or Make to build
* &dagger; [Burp Extender API](http://portswigger.net/burp/extender/api/burp_extender_api.zip) 
* &dagger; [MetaData Extractor](https://drewnoakes.com/code/exif/)
  version 2.8.1; uses Apache License v2.0
* &dagger; [XMP Library for Java](http://mvnrepository.com/artifact/com.adobe.xmp/xmpcore/5.1.2)
  version 5.1.2; uses BSD License
* Note: The [Apache Commons Imaging Library](http://commons.apache.org/proper/commons-imaging/)
  aka Sanselan) has been replaced with the MetaData Extractor.

&dagger; These items are included in the GitHub clone/fork.

The system can compile with Make.  Do in this order:

1. make clean
2. make compile
3. make

That will build the Burp plug-in and it can manually be loaded into
Burp.  Version 0.2 of the ZAP plug-in is in ZAP's GitHub repo and
included with ZAP.  To build, use Eclipse.  Version 0.3 is not fully
integrated with ZAP just yet.

# Version History

* 0.1 -
	* Initial release
	* It works
* 0.2 -
	* Added location scanning inside IPTC tags and proprietary Panasonic codes
	* Added scanning of png and tiff files
	* Replaced Sanselan with MetaData Extractor and Adobe XMP libraries
* 0.3 -
	* Fixed bugs where some codes Proprietary Camera codes were displayed as
	  ID numbers instead of text
	* Strip out tag that are \\0 null values or array of nulls
	* More testing of IPTC with good results
	* Updated to MetaData Extractor 2.9.1 for new XMP embedded in Exif tag support and
	  other bug fixes
	* Detect multiple instances of categories, for example, if there are
	  many sets of Exif GPS records, all are displayed.
	* Added display of camera serial numbers FujiFilm, Nikon, Olympus,Canon, Sigma 
	* Added display of camera owner name for Canon
	* Added support for HTML formatting in the Burp output
	* Command line version output in text or HTML formats

# Random Future Todos

* Need better testing and examples.
   * Get more IPTC test images with both location names and GPS
     positions.  ILS tests for names, but it is unknown if IPTC
     GPS works as no real world images have been provided for testing.
   * More testing with PNG & TIFF file types.  Burp and ZAP will flag
     what ever MetaData Extractor finds.
   * Donate any new test images to MetaData Extractor project for
     better cataloging.
* Currently, `make` is used to the build the system or a manual compile
  inside of Eclipse.  Use of Maven, Ant, Grails, or anything else would
  be better.  Advice needed here.  Since ZAP now builds in a more
  modern way, should migrate to that.
* There is much repeated code.  It would be better to use function
  pointers.  String of subtype, Class type, int[] of TAGS.  One of
  these days, I'll do that.
* Get the ZAP version into the mainline build; at alpha now, we need:
   1. Add i18n support, including a few translations.
   2. Custom wiki page on ZAP website.
   3. Dynamic Load() and Unload() -- is this required for passive scanners.
   4. Help file integration.
* More generalized research.  Images with embedded locations were found
  in a real-world situation with high privacy implications; thus a
  severe audit finding and the impetus for this project.  This images
  have also been seen on other sites with local expectations of privacy.
  However, we need people to try the tool when browsing sensitive sites,
  like dating or children-only social networking sites.  How pervasive
  is the issue on sensitive websites?
* White paper with better examples of "how to fix". 
* Fix HTML encoding with org.apache.commons.lang.StringEscapeUtils.escapeHTML()
  or similar.  Just in case, otherwise, a jpg could do HTML injection
  into Burp or Zap.  

Keywords: Infosec, Burp, ZAP, Audit, Information Exposure, Vulnerability, GPS, Exif, XMP, IPTC

<!--
vim: sw=4 tw=72 spell
-->

# Image Location & Privacy Scanner

Passively scans for GPS location and other privacy-related exposures in images during normal
security assessments of websites via plug-ins for both Burp & ZAP.  Image
Location & Privacy Scanner (ILS) assists in situations where end users may post profile
images and possibly give away their home location, e.g. a dating site or
children's chatroom.

More information on this topic, including a whitepaper 
based on a real-world site audit given as a presentation at the New
Jersey chapter of the OWASP organization, can be found at
[www.veggiespam.com/ils/](https://www.veggiespam.com/ils/) .

This software finds the GPS information inside of Exif tags, IPTC codes,
and proprietary camera codes. Then, the Image Location & Privacy Scanner flags the
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
The Image Location & Privacy Scanner runs as both a Burp and ZAP plug-in.
Requires:

* Burp Pro, 1.4 or newer
  [PortSwigger Burp web site](http://portswigger.net/burp/)
* ZAP, 2.4.x or newer
  [OWASP ZAP web site](https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project)

## Burp Installation

Burp Application Store: Launch Burp and click Extender tab &rarr;
Bapp Store &rarr; left pane &rarr; Image Location & Privacy Scanner.  In the right window pane, the
version and description of the plug-in will be shown; click the Install
button to download and activate.

Manual Install: Go to Extender &rarr; Extensions &rarr; Add.  Choose the
type as Java, choose the Image Location & Privacy Scanner jar file (you built or
downloaded), leave Standard Output & Error as "Show in UI" and then
click Next.  The next screen will show the "Image Location & Privacy Scanner:
plug-in version 0.4" if successful or display errors on the Error tab.
Click close to return to Burp.

Note: This is a scanner-type plug-in and the scanner is disabled in Burp
Free version.  So, the plug-in will only function inside of Burp Pro.

## ZAP Installation

The Image Location & Privacy Scanner is available as part of the alpha channel
passive scanners
in the ZAP Marketplace.  Currently, version 0.4 (plus patches) is present
in the channel and in the ZAP source code tree.
Image Location & Privacy Scanner also can be downloaded and compiled directly
into ZAP.  By default, ZAP does not process images; see the [FAQ](#faq) below.

## Sample Run

<p><img width="50%" height="50%" src="screenshot-1-burp.png" align="right"/>
Configure the web browser to proxy through Burp or ZAP per the
instructions of those products.  Then, browse to a few sample sites to
see Alerts being raised:</p>

* Sample Exif Site: [ReadExifData.com](http://readexifdata.com/)
* MetaData Extractor's [SampleOutput page](https://github.com/drewnoakes/metadata-extractor/wiki/SampleOutput)
contains some good images.  But first, in order to view the URLs
below, you may need to obtain a GitHub session cookie first by going to 
[MDE on GitHub](https://github.com/drewnoakes/metadata-extractor-images/tree/master/jpg).
    - [iPhone 4](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Apple%20iPhone%204.jpg)
    shows GPS data.
    - [FujiFilm FinePix S1 Pro](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/FujiFilm%20FinePixS1Pro%20(1).jpg)
    has embedded IPTC locations and keywords.
    - [Panasonic DMC-TZ10](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Panasonic%20DMC-TZ10.jpg) shows proprietary Panasonic tags including city, state, country along
    with facial recognition information, like the name and age of the person in
    the picture.  Burp screen shot of this shown to the right and ZAP is
	shown below.
* This professional photographer leaves Exif in many photos: [Raia.com](http://raia.com/)

<p align="center"><img width="65%" height="65%"
src="screenshot-1-zap.png" align="center"/></p>

The ILS jar file contains a `main()` function,  so it is possible to
directly run the scanner from the command line on local files.  The
classpath must contain the ILS jar file along with the supporting jars
for the MetaData Extractor and the Adobe XMP library.  To from the
command line, just do:

```
$ java ...
Java Image Location & Privacy Scanner
Usage: java ILS.class [-h|-t] file1.jpg file2.png file3.txt [...]
	-h : output results in HTML format
	-t : output results in plain text format (default)

# Call using the jar file from the Burp packaging
java -classpath image-location-scanner-all.jar com.veggiespam.imagelocationscanner.ILS file1.jpg file2.png file3.tiff

# Example command line output
Processing Panasonic DMC-TZ10.jpg :
Location Exif_GPS: 53° 8' 49.65", 8° 10' 45.1" 
Location Panasonic: City = OLDENBURG (OLDB.)
Location Panasonic: Country = GERMANY
Location Panasonic: State = OLDENBURG (OLDB.)

Privacy:: Panasonic: Face Recognition Info = Face 1: x: 142 y: 120 width: 76 height: 76 name: NIELS age: 31 years 7 months 15 days
Privacy Panasonic: Internal Serial Number = F541005110191

Processing Panasonic Lumix DMC-LX7.jpg :

Privacy Panasonic: Internal Serial Number = F111311090158

Processing j2.jpg : Location Exif_GPS: 40° 18' 54.92", -74° 39' 37.85"
```

Note the names of the jar files could be different, please confirm them.

# FAQ <a name="faq">

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
	- By default, images are not processed in ZAP, you must manually
	enabled them with: Tools &rarr; Options &rarr; Display &rarr;
	Process images in HTTP requests/responses
	- If you have images disabled in Global Exclude URL, then the
	passive image scanner, like ILS, will be unable to see the images
	and report on privacy issues.
* When I use Burp, nothing shows up
	- You probably have the display filter set to hide images, uncheck
	the box on the filter in the Targets tab.


# Build Requirements

* Java 1.6 or newer
* Gradle to build
* &dagger; [Burp Extender API](http://portswigger.net/burp/extender/api/burp_extender_api.zip) 
  1.7.13; uses proprietary license
* &dagger; [MetaData Extractor](https://drewnoakes.com/code/exif/)
  version 2.10.1; uses Apache License v2.0
* &dagger; [XMP Library for Java](http://mvnrepository.com/artifact/com.adobe.xmp/xmpcore/5.1.2)
  version 5.1.3; uses BSD License
* Note: The [Apache Commons Imaging Library](http://commons.apache.org/proper/commons-imaging/)
  aka Sanselan) has been replaced with the MetaData Extractor.

&dagger; These will be auto-fetched if you build with Gradle.

The system is built with Gradle: `gradle fatJar`

That will build the Burp plug-in and it can manually be loaded into
Burp.  Version 0.2 of the plug-in is included in ZAP's GitHub repo and
included with ZAP.  To build, use Eclipse.  Version 0.4 is not fully
integrated with ZAP just yet.  It will work with ZAP, just needs to be
properly included into the alpha/beta channels; someone can help with
that please.

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
* 0.4 -
	* New official name: *Image Location & Privacy Scanner*
	* Updated to MetaData Extractor 2.10.1 & XMP Core 6.1.10
	* Some XMP support removed via MDE; XMP tags weren't correct in some
	  cases.  Those tags will be introduced again in a future MDE
	* Removed legacy jar dependencies.
	* Build process is now Gradle only, Makefile is dead.
	* Added display of camera serial numbers for Leica, Reconyx Hyper Fire, Reconyx Ultra Fire
	* Now shows name and age of facial recognition in Panasonic cameras
* git-tip
	* Gradle build automatically downloads the Burp API jar, so no
	  need to include code in Git repo any longer
	* Fixed mixed spaces-and-tabs, thanks @kingthorn
	* Fixed a chance of an image causing HTML-injection inside of Burp;
	  I theorized it existed (maybe a non-Burp app calling ILS would
	  result in full-blown XSS against the infosec tester), but
	  @pajswigger actually exploited this type of injection in the form
	  of `<i>` tags, since Burp rejects `<script>` tags
	* Nicer Makefile (i'm sorry, i still use make)
	* Enhanced readmes, faqs, screenshots, etc.

# Random Future Todos
* Idea from Burp's @pajwigger:  It's quite common that servers return
  304 not modified. It might be a good trick, if you see a request for
  an image, and there's only 304s in the site map – that in an active
  scan you fetch the image.
* Need better testing and examples.
   * Get more IPTC test images with both location names and GPS
     positions.  ILS tests for names, but it is unknown if IPTC
     GPS works as no real world images have been provided for testing.
   * More testing with PNG & TIFF file types.  Burp and ZAP will flag
     what ever MetaData Extractor finds.
   * Donate any new test images to MetaData Extractor project for
     better cataloging.
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
* Get a Eclipse + ZAP environment working so I can test those updates
  easier.

Keywords: Infosec, Burp, ZAP, Audit, Information Exposure, Vulnerability, GPS, Exif, XMP, IPTC, PII

<!--
vim: sw=4 tw=72 spell noexpandtab
-->

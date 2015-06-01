# Image Location Scanner

Passively scans for GPS location exposure in images during normal
security assessments of websites via a Burp & ZAP plug-in.  Image
Location Scanner assists in situations where end users may post profile
images and possibly give away their home location, e.g. a dating site or
children's chatroom.

More information on this topic, including a whitepaper 
based on a real-world site audit given as a presentation at the New
Jersey chapter of the OWASP organization, can be found at
[www.veggiespam.com/ils/](http://www.veggiespam.com/ils/) .

This software finds the GPS information inside of Exif tags, IPTC codes,
and proprietary Panasonic/Lumix codes. Then, this scanner flags the
findings in the
Burp Scanner or ZAP Alerts list as an information message.  It would be
up to the auditor to determine if location exposure is truly a security
risk based on context. 

## Contributors

Special thanks to:

* The fine folks at [Aspect Security](https://www.aspectsecurity.com/) for
  performing initial tests of the alpha software and providing awesome
  feedback
* Simon Bennetts from ZAP for code reviews and help adding to the alpha
  channel

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
plug-in version 0.2" if successful or display errors on the Error tab.
Click close to return to Burp.

Note: This is a scanner-type plug-in and the scanner is disabled in Burp
Free version.  So, the plug-in will only function inside of Burp Pro.

## ZAP Installation

The Image Location Scanner is available as part of the alpha channel
in the ZAP Marketplace.  It also can be downloaded and compiled directly
into ZAP.  At this time, v0.1 is in the alpha channel and v0.2 will be
available soon.  ZAP code is also mirrored in the ZAP source tree.

## Sample Run

Configure the web browser to proxy through Burp or ZAP per the
instructions of those products.  Then, browse to a few sample sites:

* Sample Exif Site: http://readexifdata.com/ 
* MetaData Extractor has tons of examples: https://github.com/drewnoakes/metadata-extractor-images/tree/master/jpg
* Proprietary Panasonic with embedded location example:
  https://github.com/drewnoakes/metadata-extractor-images/master/jpg/Panasonic%20DMC-TZ10.jpg

To run from the jar files, it is possible to directly run the scanner on
local files.  The classpath must contain ILS along with the supporting
jars for the MetaData Extractor and the Adobe XMP library.  Then do:

`java -classpath ILS.jar:xmp.jar:mde.jar com.veggiespam.imagelocationscanner.ILS  file1.jpg file2.png file3.txt`


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

That will build the Burp plug-in.  The ZAP plug-in is not yet in the
GitHub repo and needs Eclipse to build.

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
  be better.  Advice needed here.  Perhaps do whatever ZAP does when
  they migrate off of using Ant for their build.
* Get the ZAP version into the mainline build; at alpha now, we need:
   1. Add i18n support, including a few translations.
   2. Custom wiki page on ZAP website.
   3. Dynamic Load() and Unload().
   4. Help file integration.
* More generalized research.  Images with embedded locations were found
  in a real-world situation with high privacy implications; thus a
  severe audit finding and the impetus for this project.  This images
  have also been seen on other sites with local expectations of privacy.
  However, we need people to try the tool when browsing sensitive sites,
  like dating or children-only social networking sites.  How pervasive
  is the issue on sensitive websites?
* White paper with better examples of "how to fix". 

Keywords: Infosec, Burp, ZAP, Audit, Information Exposure, Vulnerability, GPS, Exif, XMP, IPTC

<!--
vim: sw=4 tw=72 spell
-->

# Image Location and Privacy Scanner

Passively scans for GPS location and other privacy-related exposures in images during normal
security assessments of websites via plug-ins for both Burp & ZAP.  Image
Location and Privacy Scanner (ILS) assists in situations where end users may post profile
images and possibly give away their home location, e.g. a dating site or
children's chatroom.

More information on this topic, including a white paper
based on a real-world site audit given as a presentation at the New
Jersey chapter of the OWASP organization, can be found at
[www.veggiespam.com/ils/](https://www.veggiespam.com/ils/).

This software scans images to find the GPS information inside of Exif tags, IPTC codes,
and proprietary camera tags. Then, ILS flags the
findings in the
Burp Scanner or ZAP Alerts list as an information message.  It would be
up to the auditor to determine if location exposure is truly a security
risk based on context.

There are two major branches: *master* which is the mainline set of releases and *tng* which will be a next generation set of changes that may or may not compile when you clone the repo.  The master branch has tags for some released versions.

Special thanks to my [contributors, listed here](CONTRIBUTORS.md).
Full version history can be found in the [CHANGELOG.md](CHANGELOG.md) and future ideas for implementation can be found in the [TODO.md](TODO.md).

## Sample Run

<p><img width="50%" height="50%" src="img/screenshot-1-burp.png" align="right"/>
Configure the web browser to proxy through Burp or ZAP per the
instructions of those products.  Then, browse to a few sample sites to
see Alerts being raised:</p>

* MetaData Extractor's [SampleOutput page](https://github.com/drewnoakes/metadata-extractor/wiki/SampleOutput)
contains some good images.  *(Note: For some URLs, you need a [GitHub session cookie](https://github.com/drewnoakes/metadata-extractor-images/tree/master/jpg))*
    - [iPhone 4](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Apple%20iPhone%204.jpg)
    shows GPS data.
    - [FujiFilm FinePix S1 Pro](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/FujiFilm%20FinePixS1Pro%20(1).jpg)
    has embedded IPTC locations and keywords.
    - [Panasonic DMC-TZ10](https://raw.githubusercontent.com/drewnoakes/metadata-extractor-images/master/jpg/Panasonic%20DMC-TZ10.jpg) shows proprietary Panasonic MakerNote tags including city, state, country along
    with facial recognition information, like the name and age of the person in
    the picture.  Burp screen shot of this shown to the right and ZAP is
    shown below.
* This professional photographer utilizes Exif & IPTC data in many of the full-sized (non-thumbnail) photos: [Raia.com](https://raia.com/)

<p align="center"><img width="65%" height="65%"
src="img/screenshot-2-zap.png" align="center"/></p>

## Command Line Options

The ILS jar file contains a `main()` function, so it is possible to
directly run the scanner from the command line on local files.  The
classpath must contain the ILS jar file along with the supporting jars
for the MetaData Extractor.  To run from the
command line:

```bash
$ java -classpath image-location-scanner-all.jar   com.veggiespam.imagelocationscanner.ILS
Image Location and Privacy Scanner v1.2
Usage: java ILS.class [-h|-m|-t] file1.jpg file2.png file3.txt [...]
    -h : output results in semi-HTML
    -m : output results in Markdown
    -t : output results in plain text (default)
    --help : detailed help

# Run main() directly from the Burp jar packaging
$ java -classpath image-location-scanner-all.jar  com.veggiespam.imagelocationscanner.ILS [...files...]
Processing Panasonic DMC-TZ10.jpg :
  Location::
    Exif_GPS: 53° 8' 49.65", 8° 10' 45.1"
    Panasonic: City = OLDENBURG (OLDB.)
    Panasonic: Country = GERMANY
    Panasonic: State = OLDENBURG (OLDB.)
  Privacy::
    Panasonic: Face Recognition Info = Face 1: x: 142 y: 120 width: 76 height: 76 name: NIELS age: 31 years 7 months 15 days
    Panasonic: Internal Serial Number = F541005110191
Processing Panasonic Lumix DMC-LX7.jpg :
  Privacy::
    Panasonic: Internal Serial Number = F111311090158
Processing j2.jpg :
  Location::
    Exif_GPS: 40° 18' 54.92", -74° 39' 37.85"
Processing README.md : None

# With Markdown output:
$ java -classpath image-location-scanner-all.jar  com.veggiespam.imagelocationscanner.ILS -m [...files...]
# ../images/Panasonic Lumix DMC-LX7.jpg
* Privacy:: 
    * Panasonic: Internal Serial Number = F111311090158
# ../images/Sony ILCE-7M4 (A7M4).JPG
* Privacy:: 
    * Sony-Tag9050b: Internal Serial Number = 42ff00002009
# ../images/FujiFilm FinePixS1Pro (1).jpg    
* Location:: 
    * Exif_GPS: 54° 59' 22.8", -1° 54' 51"
    * IPTC: Country/Primary Location Name = 'Ubited Kingdom'
* Privacy:: 
    * IPTC: Keywords = Communications
```

Jar filenames could be different, please confirm classpath.  Yes, "Ubited" is misspelled in the sample jpg.


# Usage Requirements
The Image Location and Privacy Scanner runs as both a Burp and ZAP plug-in.
The required versions of those packages are:

* Burp Pro or Enterprise, any recent version from [PortSwigger Burp web site](https://portswigger.net/burp/Pro) - the ILS plugin does not work in the free version of Burp.
* ZAP, 2.7.x or newer from
  [OWASP ZAP web site](https://www.zaproxy.org)

## Burp Installation

***Burp Application Store:*** Launch Burp and click Extender tab &rarr;
Bapp Store &rarr; left pane &rarr; Image Location and Privacy Scanner.  In the right window pane, the
version and description of the plug-in will be shown; click the Install
button to download and activate.

***Manual Install:*** Go to Extender &rarr; Extensions &rarr; Add.  Choose the
type as Java, choose the Image Location and Privacy Scanner jar file (you built or
downloaded), leave Standard Output & Error as "Show in UI" and then
click Next.  The next screen will show the "Image Location and Privacy Scanner:
plug-in version x.x" if successful or display errors on the Error tab.
Click close to return to Burp.

🚨 **IMPORTANT** 🚨 By default, Burp hides the images and this has the side effect of also hiding any alerts detected by this plug-in.  So, you will need to enable **"Show Images"** in the filtering on the Target tab before you begin your testing.  Then, in the Target &rarr; Issues pane, you will see the privacy exposure alerts raised by the Image Location and Privacy Scanner plug-in.

Note: This is a scanner-type plug-in and the scanner is disabled in Burp
Free version.  So, the plug-in will only function inside of Burp Pro.

## ZAP Installation

The Image Location and Privacy Scanner is available in the ZAP Marketplace (beta channel).  Click the Add-On icon (<img src="img/zap-img-plugin-block.png">)  &rarr; Marketplace and filter on "image". Enable the checkbox in the "Selected" column and press "Install Selected" at the bottom.  ZAP will show ILS as "Version 6.0.0" which corresponds to ILS v1.2.

Image Location and Privacy Scanner also can be built locally and installed via File &rarr; "Load Add-On File".

🚨 **IMPORTANT** 🚨 By default, ZAP hides the images.  So, you must manually enabled image scanning with: Tools &rarr; Options &rarr; Display &rarr; "Process images in the HTTP requests/responses".  If you have images disabled in ZAP's Global Exclude URL feature, the ILS will be unable to see the images and report on privacy issues - thus disuse this feature with images so ILS can function.

# <a name="faq"> FAQ
* When I use Burp or ZAP, no issues are displayed
	- By default, both hide images in general usage. As such, enable image display using the directions above.
* Why do I see two sets of Exif_GPS coordinates (or another tag)
	- This means the image has been embedded with multiple Exif tags of
	the same type.  Thus more than one GPS location can appear.  The ILS
	software displays all that are detected.
* What types of image files are scanned, why don't you scan type X
  - Currently, ILS scans: "jpeg", "jpg", "png", "heif" extensions and mime types
  - ILS could possibly find leaks in "raw" or "psd" (Photoshop), but those files 1) are generally not displayed in-line on a browser and 2) can be huge and would start slowing down Burp - but Burp is already reading them.  TBD.
  - We generally don't see embedded leakage data in "gif", so we don't scan those.
* I see GPS location and altitude, but where is the speed, bearing, reference data, etc
  - We decided to not display all the GPS data, simply the location and altitude.  Submit a patch if you need all GPS info.
* You missed the serial number for Camera Type X
	- Could be true.  This information exposure list was built by
	manually looking through all "Makernote" tags available in MDE.  If something new was
	added, then ILS needs to also account for it.  File a bug report
	[on GitHub](https://github.com/veggiespam/ImageLocationScanner/issues)
  and we will update in a future release.
* Another Exif scanner says `City = ` with no city listed
	- It actually says "City = \\0\\0\\0\\0\\0 ..." with maybe 64 nulls.
	In newer versions ILS, we simply filter out strings that start with
	a null character.  We assume someone isn't hiding data after the first null.
* Another Exif scanner says `City = ---` but ILS does not show this value.
  - Some cameras and devices, like Panasonic, place "---" into fields 
    where there is no value or the value is unknown.  Other examples observed are
    "Off" when there is no data entered into the text field or a feature is
    inactive or a single space for a name or "-".
    ILS just filters these fields from the display since there is no location or privacy leakage.

## Build Requirements

* Java 1.9 or newer
* Gradle 1.6 or newer to build
* &dagger; [Burp Extender API](http://portswigger.net/burp/extender/api/burp_extender_api.zip)
  2.3; uses proprietary license
* &dagger; [MetaData Extractor](https://drewnoakes.com/code/exif/)
  version 2.19.0; uses Apache License v2.0
&dagger; These will be auto-fetched if you build with Gradle.

The Burp plug-in is built with Gradle: `gradle fatJar` (or be lazy and type `make`). After building, the plug-in can manually be loaded into Burp.  

To build for ZAP, it is easiest start by forking [ZAP Extensions](https://github.com/zaproxy/zap-extensions) or [my outdated repo](https://github.com/veggiespam/zap-extensions).  Then, overwrite your repo's ILS.java with the updated version.  Compile with `./gradlew :addOns:imagelocationscanner:build` and install *imagelocationscanner-{id}.zap* add-on file into ZAP.

Keywords: Infosec, Burp, ZAP, Audit, Information Exposure, Data Leakage, Vulnerability, GPS, Exif, IPTC, PII, OpSec, Privacy

<!--
vim: sw=4 ts=4 sts=4 spell expandtab
-->

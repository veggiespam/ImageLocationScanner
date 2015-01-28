# Image Location Scanner

Passively scans for GPS location exposure in images during normal
security assessments of websites via a Burp & ZAP plug-in.  Image
Location Scanner assists in situations where end users may post profile
images and possibly give away their home location, e.g. a dating site or
children's chatroom.

A whitepaper will be published soon based on a real-world site audit.

This software works well, finds the GPS information, flags it in the
Burp Scanner or ZAP Alerts list as an information message.  It would be
up to the auditor to determine if location exposure is truly a security
risk based on context.  The organization of the code, however, is
subject to change.  Java classnames might be munged about, the ZAP code
needs to be added to the repo once it is fully plug-in-ized, etc.  


# Usage Requirements
The ImageLocationScanner runs as both a Burp and ZAP plug-in.  Just
download the correct installer.

* Burp Free or Pro, 1.4 or newer
  http://portswigger.net/burp/
* ZAP, 2.3.x or newer
  https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project

## Burp Installation

Burp Application Store: Launch Burp and go to the tabs: Extender &rarr;
Bapp Store &rarr; left pane &rarr; Image Location Scanner.  In the right window pane, the
version and description of the plug-in will be shown; click the Install
button to download and activate.

Manual Install: Go to Extender &rarr; Extensions &rarr; Add.  Choose the
type as Java, choose the Image Location Scanner jar file (you built or
downloaded), leave Standard Output & Error as "Show in UI" and then
click Next.  The next screen will show the "Image Location Scanner:
plug-in version 0.1" if successful or display errors on the Error tab.
Click close to return to Burp.

## ZAP Installation

At this point, the plug-in must be compiled directly into ZAP.  This will
get better in the future.  It better get better.

## Sample Run

* Command line: `java -classpath ILS.jar
  com.veggiespam.imagelocationscanner.ILS  file1.jpg file2.png
  file3.txt`
* Testing website: http://readexifdata.com/ 


# Build Requirements

* Java 1.6 or newer
* Eclipse or Make to build
* &dagger; [Apache Commons Imaging Library](http://commons.apache.org/proper/commons-imaging/)
  (formerly known as Sanselan), version 0.97 or newer
* &dagger; [Burp Extender API](http://portswigger.net/burp/extender/api/burp_extender_api.zip) 

&dagger; These items are included in the GitHub clone/fork.

The system can compile with Make.  Do in this order:

1. make clean
2. make compile
3. make

That will build the Burp plug-in.  The ZAP plug-in is not yet in the
GitHub repo and needs Eclipse to build.

Keywords: Infosec, Burp, ZAP, Audit, Information Exposure, Vulnerability

<!--
vim: sw=4 tw=72 spell
-->

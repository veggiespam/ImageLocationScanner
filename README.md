# ImageLocationScanner

Scan for GPS location exposure in images with this Burp & ZAP plug-in.  This
assists with security audits of systems that allow users to post images.

This software works well, but is quite *alpha* in the organization of
the system and code.  Java classnames are subject to change, etc.  

# Usage Requirements
The ImageLocationScanner runs as both a Burp and ZAP plug-in.  Just
download the correct installer.

* Burp Free or Pro, 1.4 or newer
  http://portswigger.net/burp/
* ZAP, 2.4.x or newer
  https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project

## Burp Installation

Launch Burp and go to Extender &rarr; Bapp Store &rarr; Image Location
Scanner.  Then click Install.

## ZAP Installation

At this point, the plugin must be compiled directly into ZAP.  This will
get better in the future.

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
repo and needs eclipse to build.

Keywords: Infosec, Burp, ZAP, Audit, Information Exposure

<!--
vim: sw=4 tw=72 spell
-->

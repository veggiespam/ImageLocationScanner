# Random & Future Todos

## Functionality, Features, & Code
* Idea from Burp's @pajwigger:  It's quite common that servers return 304 not modified. It might be a good trick, if you see a request for an image, and there's only 304s in the site map – that in an active scan you fetch the image.
* Have a configuration option to only show alerts if the Exif or other location is in a selective area, like a certain country or range of GPS coordinates. Otherwise, ignore the image. [enhancement suggestion #10](https://github.com/veggiespam/ImageLocationScanner/issues/10)
* Need better testing and examples.
	* Get more IPTC test images with both location names and GPS positions.  ILS tests for names, but it is unknown if IPTC GPS works as no real world images have been provided for testing.
	* More testing with PNG & TIFF file types.  Burp and ZAP will flag what ever MetaData Extractor finds.
	* Donate any new test images to MetaData Extractor project for better cataloging.
* XMP scanner support was removed from MDE as processing [was not reliable](https://github.com/drewnoakes/metadata-extractor/commit/5b07a49f7b3d90c43a36a79dc4f6474845e1ebc7).  Since some drones embedded GPS information tags via XMP, it would be good to add support back once MDE adds it.  There is an XMP tag for TAG_CAMERA_SERIAL_NUMBER too.
* We don't scan many file types, like gif, psd, or raw camera files as they generally don't have leaked data (gif)
 or mostly downloaded and not embedded into a page (psd / raw).  So, perhaps a command-line file scanner works better.  But, ILS can scan raw; have it as a configuration option?  Other types too?
* There is much repeated code.  It would be better to use function pointers.  String of subtype, Class of camera type, int[] of TAGS.  One of these days, I'll do that.  There is some non-working commented out code that experiments with this.

## Research & Help
* More generalized research.  Images with embedded locations were found in a real-world situation with high privacy implications; thus a severe audit finding and the impetus for this project.  This images have also been seen on other sites with local expectations of privacy.  However, we need people to try the tool when browsing sensitive sites, like dating or children-only social networking sites.  How pervasive is the issue on sensitive websites?
* White paper with better examples of "how to fix".

## ZAP & Burp
* Migrate ILS to the new Burp Montoya API from current Burp legacy API
* Get the ZAP version into the mainline build; at beta now, we need:
	1. Add i18n support, including a few translations.
	2. Custom wiki page on ZAP website.
	3. Dynamic Load() and Unload() -- is this required for passive scanners.
	4. Help file integration.
* For unit tests inside of the ZAP integration, add more test images for various cameras and location exposure, maybe a loop checking for this.
* For unit tests inside of the ZAP integration, create a test which uses different content types.
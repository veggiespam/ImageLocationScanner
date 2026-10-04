package com.veggiespam.imagelocationscanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;		// for debugging only
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.HashMap;
import java.util.TimeZone;

import com.adobe.internal.xmp.XMPException;
import com.adobe.internal.xmp.XMPIterator;
import com.adobe.internal.xmp.XMPMeta;
import com.adobe.internal.xmp.options.IteratorOptions;
import com.adobe.internal.xmp.properties.XMPPropertyInfo;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.lang.GeoLocation;
import com.drew.metadata.TagDescriptor;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifDescriptorBase;
import com.drew.metadata.exif.GpsDirectory;
import com.drew.metadata.iptc.IptcDirectory;
import com.drew.metadata.xmp.XmpDirectory;
import com.drew.metadata.iptc.IptcDescriptor;
import com.drew.metadata.exif.makernotes.PanasonicMakernoteDirectory;
import com.drew.metadata.exif.makernotes.PanasonicMakernoteDescriptor;
import com.drew.metadata.exif.makernotes.LeicaMakernoteDirectory;
import com.drew.metadata.exif.makernotes.LeicaMakernoteDescriptor;
import com.drew.metadata.exif.makernotes.ReconyxUltraFireMakernoteDirectory;
import com.drew.metadata.exif.makernotes.SamsungType2MakernoteDescriptor;
import com.drew.metadata.exif.makernotes.SamsungType2MakernoteDirectory;
import com.drew.metadata.exif.makernotes.ReconyxUltraFireMakernoteDescriptor;
import com.drew.metadata.exif.makernotes.ReconyxHyperFireMakernoteDirectory;
import com.drew.metadata.exif.makernotes.ReconyxHyperFireMakernoteDescriptor;
import com.drew.metadata.exif.makernotes.ReconyxHyperFire2MakernoteDirectory;
import com.drew.metadata.exif.makernotes.ReconyxHyperFire2MakernoteDescriptor;
import com.drew.metadata.exif.makernotes.CanonMakernoteDirectory;
import com.drew.metadata.exif.makernotes.CanonMakernoteDescriptor;
import com.drew.metadata.exif.makernotes.SigmaMakernoteDirectory;
import com.drew.metadata.exif.makernotes.SonyTag9050bDescriptor;
import com.drew.metadata.exif.makernotes.SonyTag9050bDirectory;
import com.drew.metadata.exif.makernotes.SigmaMakernoteDescriptor;
import com.drew.metadata.exif.makernotes.NikonType2MakernoteDirectory;
import com.drew.metadata.exif.makernotes.NikonType2MakernoteDescriptor;
import com.drew.metadata.exif.makernotes.OlympusMakernoteDirectory;
import com.drew.metadata.exif.makernotes.OlympusMakernoteDescriptor;
import com.drew.metadata.exif.makernotes.OlympusEquipmentMakernoteDirectory;
import com.drew.metadata.exif.makernotes.OlympusEquipmentMakernoteDescriptor;
import com.drew.metadata.exif.makernotes.FujifilmMakernoteDirectory;
import com.drew.metadata.exif.makernotes.FujifilmMakernoteDescriptor;
import com.drew.metadata.png.PngDirectory;
import com.drew.metadata.png.PngDescriptor;
import com.drew.metadata.file.FileSystemDirectory;


/**
 * Image Location and Privacy Scanner main static class.  Passively scans an
 * image data stream (jpg/png/etc) and reports if the image contains embedded
 * location or privacy information, such as Exif GPS, IPTC codes, and some
 * proprietary camera codes which may contain things like serial numbers.  This
 * class is designed to be a plug-in for both ZAP and Burp proxies.
 * 
 * @author Jay Ball | github: @veggiespam | linktr.ee/veggiespam | https://www.veggiespam.com/ils/
 * @license Apache License 2.0
 * @version 2.0-ALPHA
 * @see https://www.veggiespam.com/ils/
 */
public class ILS {

	/** A bunch of static strings that are used by both ZAP and Burp plug-ins. */
	public static final String pluginName = "Image Location and Privacy Scanner";

	public static final String pluginVersion = "2.0-ALPHA";
	public static final String alertTitle = "Image Exposes Location or Privacy Data";
	public static final String alertDetailPrefix = "This image embeds a location or leaks privacy-related data: ";
	public static final String alertBackground 
		= "The image was found to contain embedded location information, such as GPS coordinates, or "
		+ "another privacy exposure, such as camera serial number.  "
		+ "Depending on the context of the image in the website, "
		+ "this information may expose private details of the users of a site.  For example, a site that allows "
		+ "users to upload profile pictures taken in the home may expose the home's address.  ";
	public static final String remediationBackground 
		= "Before allowing images to be stored on the server and/or transmitted to the browser, strip out the "
		+ "embedded location information from image.  This could mean removing all Exif data or just the GPS "
		+ "component.  Other data, like serial numbers, should also be removed.";
	public static final String remediationDetail = null;
	public static final String referenceURL = "https://www.veggiespam.com/ils/"; 
	public static final String pluginAuthor = "Jay Ball (@veggiespam) www.veggiespam.com"; 

	private static final String EmptyString = "";
	private static final String TextSubtypeEnd = ": "; // colon space for plain text results

	private static final String HTML_subtype_begin = "<li>";
	private static final String HTML_subtype_title_end = "\n\t<ul>\n";
	private static final String HTML_subtype_end = "\t</ul></li>\n";

	private static final String HTML_finding_begin = "\t<li>";
	private static final String HTML_finding_end = "</li>\n";

	// Used in the results array and elsewhere, values are an index.
	public static enum OutputFormat { 
		out_text,				// == 0
		out_html, 				// == 1
		out_md  				// == 2
	};

	public ILS() {
		// blank constructor
		super();
	}

	public String getAuthor() {
		return pluginAuthor;
	}

	private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
	/**
	 * Converts byte array to hex String, prepending "0x" to the result, similar to Apache Commons code.  Probably should be replaced with org.apache.commons.codec.binary.Hex someday.  This code is used only for debugging.  Maybe java.util.HexFormat as suggested in newer Java versions.
	 * 
	 * @param bytes the byte array
	 * @return the hexadecimal representation of the byte array
	 * @see https://stackoverflow.com/questions/9655181/java-convert-a-byte-array-to-a-hex-string (first answer for code, 
	 */
	private static String byteArrayToHex(byte[] bytes) {
		if (bytes == null || bytes.length == 0)
			return EmptyString;
		char[] hexChars = new char[bytes.length * 2 + 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return "0x" + new String(hexChars);
	}

	private static class ScanResultEntry {
		public String tagType;
		public ArrayList<String> findings;

		public ScanResultEntry() {
			this.tagType = EmptyString;
			this.findings = new ArrayList<String>();
		}
		public ScanResultEntry(String tagType) {
			this.tagType = tagType;
			this.findings = new ArrayList<String>();
		}
		public void addFinding(String finding) {
			this.findings.add(finding);
		}
		public ArrayList<String> getFindings() {
			return this.findings;
		}
		@Override
		public String toString() {
			return toText();
		}
		public String toText() {
			StringBuilder sb = new StringBuilder();
			sb.append("  ");
			sb.append(tagType);
			sb.append(" - " +findings.size());
			sb.append("\n");
			for (String finding : findings) {
				sb.append("    ");
				sb.append(escapeTEXT(finding));
				sb.append("\n");
			}
			return sb.toString();
		}
		public String toHTML() {
			StringBuilder sb = new StringBuilder();
			sb.append(HTML_subtype_begin);
			sb.append(tagType);
			sb.append(HTML_subtype_title_end);
			for (String finding : findings) {
				sb.append(HTML_finding_begin);
				sb.append(escapeHTML(finding));
				sb.append(HTML_finding_end);
			}
			sb.append(HTML_subtype_end);
			return sb.toString();
		}
		public String toMarkdown() {
			StringBuilder sb = new StringBuilder();
			sb.append("### " + tagType + "\n");
			for (String finding : findings) {
				sb.append("* " + escapeMD(finding) + "\n");
			}
			return sb.toString();
		}
	}


	/** Tests a data blob for Location or GPS information and returns the image location
	 * information as a string.  If no location is present or there is an error,
	 * the function will return an empty string of "".
	 * 
	 * @param data is a byte array that is an image file to test, such as entire jpeg file.
	 * @param outputtype specifies the format of the output (text, HTML, or Markdown).
	 * @return String containing the Location data or an empty String indicating no GPS data found.
	 */
	public static String scanForLeaksInDataBlob(byte[] data, OutputFormat outputtype)   {
		StringBuilder res = new StringBuilder();

		// Extreme debugging code for making sure data from Burp/ZAP/new-proxy gets into 
		// ILS.  This code is very slow and not to be compiled in, even with if(debug)
		// types of constructs.  This code this will save the image file to disk for binary
		// import debugging.  ---  change that false to true to enable this block.

		// Code assumes the image is a jpg and uses that extension, even if the actual image format is different.
		/*
		if (false) {
			String t[] = { EmptyString, EmptyString, EmptyString };
			try{
				TimeZone tz = TimeZone.getTimeZone("UTC");
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
				df.setTimeZone(tz);
				String nowAsISO = df.format(new Date());
				FileOutputStream o = new FileOutputStream(new File("/tmp/ILS-debug-" + nowAsISO + ".jpg"));
				o.write(data);
				o.close();
			} catch (IOException e) {
				t[0] = "IOException Exception " + e.toString();
				t[1] = t[0];
				t[2] = t[0];
				return t;
			}
			t[0] = "Scanning " + data.length + "\n\n";
			t[1] = t[0];
			t[2] = t[0];
			// return t;   
			//   --- if you use this return line, remember to comment out rest of function.
		}
		*/
		
		try {
			BufferedInputStream is = new BufferedInputStream(new ByteArrayInputStream(data, 0, data.length));
			Metadata md = ImageMetadataReader.readMetadata(is);

			ArrayList<ScanResultEntry> results;
			results = scanForLeaks(md);

			for (ScanResultEntry entry : results) {
				switch (outputtype) {
					case out_text:
						res.append(entry.toText());
						break;
					case out_html:
						res.append(entry.toHTML());
						break;
					case out_md:
						res.append(entry.toMarkdown());
						break;
				}
			}


		} catch (ImageProcessingException e) {
			// bad image, just ignore processing exceptions
			// return "ImageProcessingException: " + e.toString();	
		} catch (IOException e) {
			// bad file or something, just ignore 
			// return "IOException: " + e.toString();
		}

		return res.toString(); 
	}


	/** Returns ILS information as HTML formatting string.
	 * 
	 * @see scanForLeaksInDataBlob
	 */
	public static String scanForLocationInImageHTML(byte[] data)   {
		return scanForLeaksInDataBlob(data, OutputFormat.out_html);
	}

	/** Returns ILS information as Text formatting string.
	 * 
	 * @see scanForLeaksInDataBlob
	 */
	public static String scanForLocationInImageText(byte[] data)   {
		return scanForLeaksInDataBlob(data, OutputFormat.out_text);
	}

	/** Returns ILS information as Markdown formatting string.
	 * 
	 * @see scanForLeaksInDataBlob
	 */
	public static String scanForLocationInImageMD(byte[] data)   {
		return scanForLeaksInDataBlob(data, OutputFormat.out_md);
	}

	/** Returns ILS information in Text or HTML or Markdown depending on outputtype flag.
	 * 
	 * @param data is a byte array that is an image file to test, such as entire jpeg file.
	 * @param outputtype output results as plain text, html, or markdown.
	 * @return String containing the Location data or an empty String indicating no GPS data found.
	 * @see scanForLeaksInDataBlob
	 */
	public static String scanForLocationInImage(byte[] data, OutputFormat outputtype)   {
		switch (outputtype) {
			case out_text:
				return scanForLocationInImageText(data);
			case out_html:
				return scanForLocationInImageHTML(data);
			case out_md:
				return scanForLocationInImageMD(data);
			default:
				return scanForLocationInImageText(data);
		}
	}


	/** Do this for completeness, even if a no-op for now. */
	private static String escapeTEXT(String s) {
		return s;  // might want to do more here someday, like binary data as hex codes, etc...
	}

	/** Theoretical chance of XSS inside of Burp/ZAP, so return properly escaped HTML. */
	private static String escapeHTML(String s) {
		return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
	}

	/** Since MD allows HTML directly, escape as HTML.  Probably needs more work. */
	private static String escapeMD(String s) {
		return escapeHTML(s);
	}

	/** Determines if a Makernote tag value has a real value, not blank or contains a placeholder.
	 * 
	 *	@param tag String of Makernote tag to examine
	 *	@return true if the tag is meaningful; false otherwise
	 */
	private static boolean isTagMeaningful(String tag) {
		if (null == tag) 
			return false;
		tag = tag
			.trim()			  // trim off whitespace - may leave us with EmptyString, which is gets tested below.
			.toLowerCase();	  // Panasonic likes "Off", others like "off"
		if (	tag.equals(EmptyString) 
				|| tag.equals("---") 
				|| tag.equals("-") 
				|| tag.equals("off") 
				|| tag.charAt(0) == '\0'	// some tag values are simply all null characters, test first one, good enough
			) {
			return false;
		}
		return true;
	}

    private static boolean isTagInTagList(int[] arr, int key) {
        for (int element : arr) {
            if (element == key) {
                return true;
            }
        }
        return false;
    }

	private static final HashMap<Class,int[]> class_tag_set;
	private static final Set<String> xmp_tag_set;

	static {
		class_tag_set = new HashMap<>();

		class_tag_set.put(
				ExifDirectoryBase.class, new int[] {
					ExifDirectoryBase.TAG_IMAGE_UNIQUE_ID,
					ExifDirectoryBase.TAG_IMAGE_DESCRIPTION,
					ExifDirectoryBase.TAG_CAMERA_OWNER_NAME,
					ExifDirectoryBase.TAG_BODY_SERIAL_NUMBER,
					ExifDirectoryBase.TAG_LENS_MAKE,
					ExifDirectoryBase.TAG_LENS_MODEL,
					ExifDirectoryBase.TAG_LENS_SERIAL_NUMBER,
					ExifDirectoryBase.TAG_USER_COMMENT,
					ExifDirectoryBase.TAG_WIN_COMMENT,		
					ExifDirectoryBase.TAG_WIN_TITLE,
					ExifDirectoryBase.TAG_WIN_COMMENT, // Oddly, I've seen GPS leaks here.
					ExifDirectoryBase.TAG_WIN_AUTHOR,
					ExifDirectoryBase.TAG_WIN_KEYWORDS,
					ExifDirectoryBase.TAG_WIN_SUBJECT
				}
		);
		class_tag_set.put(
				SonyTag9050bDirectory.class, new int[] {
					SonyTag9050bDirectory.TAG_INTERNAL_SERIAL_NUMBER
				}
		);
		class_tag_set.put(
				SigmaMakernoteDirectory.class, new int[] {
					SigmaMakernoteDirectory.TAG_SERIAL_NUMBER
				}
		);
		class_tag_set.put(
				IptcDirectory.class, new int[] {
					IptcDirectory.TAG_KEYWORDS,
					IptcDirectory.TAG_LOCAL_CAPTION,
					IptcDirectory.TAG_CITY,
					IptcDirectory.TAG_SUB_LOCATION,
					IptcDirectory.TAG_PROVINCE_OR_STATE,
					IptcDirectory.TAG_CONTENT_LOCATION_CODE,
					IptcDirectory.TAG_CONTENT_LOCATION_NAME,
					IptcDirectory.TAG_COUNTRY_OR_PRIMARY_LOCATION_CODE,
					IptcDirectory.TAG_COUNTRY_OR_PRIMARY_LOCATION_NAME,
					IptcDirectory.TAG_DESTINATION,
				}
		);
		class_tag_set.put(
				LeicaMakernoteDirectory.class, new int[] {
					LeicaMakernoteDirectory.TAG_SERIAL_NUMBER
				}
		);
		class_tag_set.put(
				CanonMakernoteDirectory.class, new int[] {
					CanonMakernoteDirectory.TAG_CANON_OWNER_NAME,
					CanonMakernoteDirectory.TAG_CANON_SERIAL_NUMBER
				}
		);
		class_tag_set.put(
				FujifilmMakernoteDirectory.class, new int[] {
					FujifilmMakernoteDirectory.TAG_SERIAL_NUMBER
				}
		);
		class_tag_set.put(
				PanasonicMakernoteDirectory.class, new int[] {
					PanasonicMakernoteDirectory.TAG_BABY_AGE,
					PanasonicMakernoteDirectory.TAG_BABY_AGE_1,
					PanasonicMakernoteDirectory.TAG_BABY_NAME,
					PanasonicMakernoteDirectory.TAG_FACE_RECOGNITION_INFO,
					PanasonicMakernoteDirectory.TAG_INTERNAL_SERIAL_NUMBER,
					PanasonicMakernoteDirectory.TAG_LENS_SERIAL_NUMBER,
					PanasonicMakernoteDirectory.TAG_TEXT_STAMP,
					PanasonicMakernoteDirectory.TAG_TEXT_STAMP_1,
					PanasonicMakernoteDirectory.TAG_TEXT_STAMP_2,
					PanasonicMakernoteDirectory.TAG_TEXT_STAMP_3,
					PanasonicMakernoteDirectory.TAG_TITLE,
					PanasonicMakernoteDirectory.TAG_CITY,
					PanasonicMakernoteDirectory.TAG_COUNTRY,
					PanasonicMakernoteDirectory.TAG_LANDMARK,
					PanasonicMakernoteDirectory.TAG_LOCATION,
					PanasonicMakernoteDirectory.TAG_STATE,
					PanasonicMakernoteDirectory.TAG_WORLD_TIME_LOCATION  // might expose timezone aka location - but I only see value "HOME" in my samples.
				}
		);
		class_tag_set.put(
				NikonType2MakernoteDirectory.class, new int[] {
					NikonType2MakernoteDirectory.TAG_CAMERA_SERIAL_NUMBER,
					NikonType2MakernoteDirectory.TAG_CAMERA_SERIAL_NUMBER_2
				}
		);
		class_tag_set.put(
				OlympusMakernoteDirectory.class, new int[] {
					OlympusMakernoteDirectory.TAG_SERIAL_NUMBER_1,
					OlympusMakernoteDirectory.TAG_SERIAL_NUMBER_2
				}
		);
		class_tag_set.put(
				OlympusEquipmentMakernoteDirectory.class, new int[] {
					OlympusEquipmentMakernoteDirectory.TAG_SERIAL_NUMBER,
					OlympusEquipmentMakernoteDirectory.TAG_INTERNAL_SERIAL_NUMBER,
					OlympusEquipmentMakernoteDirectory.TAG_LENS_SERIAL_NUMBER,
					OlympusEquipmentMakernoteDirectory.TAG_EXTENDER_SERIAL_NUMBER,
					OlympusEquipmentMakernoteDirectory.TAG_FLASH_SERIAL_NUMBER
				}
		);
		class_tag_set.put(
				ReconyxHyperFire2MakernoteDirectory.class, new int[] {
					ReconyxHyperFire2MakernoteDirectory.TAG_SERIAL_NUMBER,
					ReconyxHyperFire2MakernoteDirectory.TAG_USER_LABEL
				}
		);
		class_tag_set.put(
				ReconyxUltraFireMakernoteDirectory.class, new int[] {
					ReconyxUltraFireMakernoteDirectory.TAG_SERIAL_NUMBER,
					ReconyxUltraFireMakernoteDirectory.TAG_USER_LABEL
				}
		);
		class_tag_set.put(
				SamsungType2MakernoteDirectory.class, new int[] {
					SamsungType2MakernoteDirectory.TagSerialNumber,
					SamsungType2MakernoteDirectory.TagFaceName,
					SamsungType2MakernoteDirectory.TagInternalLensSerialNumber,
					SamsungType2MakernoteDirectory.TagEncryptionKey
				}
		);
		class_tag_set.put(PngDirectory.class, new int[] {
			PngDirectory.TAG_TEXTUAL_DATA
		});
			


		// Some of these results are Strings, others are fancy binary values.
		xmp_tag_set = Set.of(
			"dc:description",
			"drone-dji:AbsoluteAltitude",
			"drone-dji:GpsLatitude",
			"drone-dji:GpsLongitude",
			"drone-dji:RelativeAltitude",
			"exif:GPSAltitude",
			"exif:GPSDestLatitude",
			"exif:GPSDestLongitude",
			"exif:GPSLatitude",
			"exif:GPSMapDatum",
			"exif:ImageUniqueID",
			"exif:UserComment",
			"exifEX:BodySerialNumber",
			"exifEX:CameraOwnerName",
			"exifEX:LensMake",
			"exifEX:LensModel",
			"exifEX:LensSerialNumber"
		);
	}; // end static definition

	/** Test the Metadata for privacy leaks, generally the file stream or other processor calls this.  Returns ... .
	 * 
	 * @param md is a Metadata object that represents the image file to test, such as entire jpeg file.
	 * @return String Array containing the Location data or an empty String indicating no GPS data found.
	 */
	public static ArrayList<ScanResultEntry> scanForLeaks(Metadata md)   {
	
		ArrayList<ScanResultEntry> scanResultsList = new ArrayList<ScanResultEntry>();

		for (Directory dir : md.getDirectories()) {
			String directoryName = dir.getName();

			if (dir.getTagCount() == 0)
				continue;

			/*  // dump all tags for debug fun
			for (Tag tag : dir.getTags()) {
				String tagName = tag.getTagName();
				String description;
				try {
					description = tag.getDescription();
				} catch (Exception ex) {
					description = "ERROR: " + ex.getMessage();
				}
				if (description == null)
					description = "";
				System.out.println(directoryName + " - " + tagName + " = " + description + " (length: " + description.length() + ")");
			}*/
			


			// Because some Exif directories are basically duplicates, sub-implementations, or have overlap with of ExifDirectoryBase,  the ExifDirectoryBase tags can appear in any of them.  So, containsKey() detects "Exif ID0", the subclass of ExifDirectoryBase, and fails.  Thus, we need to do instaceof check also.  Later, we also set tag_set correctly.

			if ((dir instanceof ExifDirectoryBase) || class_tag_set.containsKey(dir.getClass())) {
				ScanResultEntry res = new ScanResultEntry(directoryName);
				int[] tag_set = (dir instanceof ExifDirectoryBase) 
						? class_tag_set.get(ExifDirectoryBase.class)
						: class_tag_set.get(dir.getClass());

				for (Tag tag : dir.getTags()) {
					int tagType = tag.getTagType();

					if (! isTagInTagList(tag_set, tagType)) {
						continue;
					}

					String tagName = tag.getTagName();
					String description;

					try {
						description = (dir instanceof ExifDirectoryBase) 
								? ((ExifDirectoryBase)dir).getDescription(tagType)
								: tag.getDescription();
						//description = tag.getDescription();
					} catch (Exception ex) {
						description = "ERROR getting " + tagName + " description: " + ex.getMessage();
					}

					/* Debugging for some binary tags. 
					if (tagType == ExifDirectoryBase.TAG_USER_COMMENT) {
						System.out.println("User Comment: " 
							+ byteArrayToHex(dir.getByteArray(ExifDirectoryBase.TAG_USER_COMMENT)) + " characters");
					}
					*/
					if (description == null)
						continue; // instead of calling "is this meaningful" since we know it isn't
					
					if ( isTagMeaningful(description) ) {
						res.addFinding(tagName + " = " + description );
					}
				}

				if (res.getFindings().size() > 0) {
					scanResultsList.add(res);
				}
			}

			if (dir instanceof GpsDirectory) {
				GpsDirectory gpsDir = (GpsDirectory)dir;
				ScanResultEntry res = new ScanResultEntry("GPS in Exif");

				GeoLocation geoLocation = gpsDir.getGeoLocation();
				if ( ! (geoLocation == null || geoLocation.isZero()) ) {
					String finding = "Lat/Long = " + geoLocation.toDMSString();

					res.addFinding(finding);
				}

				String alt = gpsDir.getDescription(GpsDirectory.TAG_ALTITUDE);
				String altref = gpsDir.getDescription(GpsDirectory.TAG_ALTITUDE_REF);
				if (alt != null || altref != null) {
					String finding = "Altitude = " + (alt == null ? "<unknown elevation>" : alt) 
										    + " " + (altref == null ? EmptyString : altref);
					res.addFinding(finding);
				}

				if (res.getFindings().size() > 0) {
					scanResultsList.add(res);
				}
			}
			
			if (dir instanceof XmpDirectory) {
				XmpDirectory xmpDir = (XmpDirectory)dir;
				ScanResultEntry res = new ScanResultEntry(directoryName);

				try {				
					XMPMeta xmpMeta = xmpDir.getXMPMeta();
					IteratorOptions options = new IteratorOptions().setJustLeafnodes(true);
					XMPIterator iterator = xmpMeta.iterator(options);
					while (iterator.hasNext()) {
						XMPPropertyInfo prop = (XMPPropertyInfo)iterator.next();
						String value = prop.getValue();

						if (value == null)
							continue; // missing values are not meaningful, unless perchance the path is a leak, but doubtful

						String path = prop.getPath();
						if (path == null) {
							// path = "Blank XMP path";
							continue; // if we have a desire to report on blank XMP paths, would also need to modify if (xmp_tag_set.contains(path)) check below
						}

						String ns = prop.getNamespace();
						if (ns == null || ns.isEmpty())
							ns = "<null or empty>";
						if (xmp_tag_set.contains(path)) {
							if ( isTagMeaningful(value) ) {
								res.addFinding(path + " = " + value + " (ns='" + ns + "')" );
							}
						}
					}
				} catch (XMPException e) {
					throw new RuntimeException("XMP processing error", e);
				}
				if (res.getFindings().size() > 0) {
					scanResultsList.add(res);
				}

			}
		}
		return scanResultsList;
	}

	// Each paragraph is an array entry
	public static final String CommandLineHelp[] = {
		"Copyright © Jay Ball (@veggiespam)"
		,
		"See github.com/veggiespam/ImageLocationScanner - license Apache 2.0"
		,
		"Passively scans for GPS location and other privacy-related exposures in images during normal security assessments of websites; this jar is also a plug-in for both Burp & ZAP.  Image Location and Privacy Scanner (ILS) assists in situations where end users may post profile images and possibly give away their home location, e.g. a dating site or children's chatroom."
		,
		"More information on this topic, including a white paper based on a real-world site audit given as a presentation at the New Jersey chapter of the OWASP organization, can be found at https://www.veggiespam.com/ils/"
		,
		"This software scans images to find the GPS information inside of Exif tags, IPTC codes, and proprietary camera tags. It also detects other privacy-related exposures such as camera serial numbers, facial recognition data, and other metadata that could compromise user privacy." 
	};

	public static void main(String[] args) throws Exception {
		OutputFormat outputtype = OutputFormat.out_text;
		if (args.length == 0){
			System.out.println("Image Location and Privacy Scanner v" + pluginVersion);
			System.out.println("Usage: java ILS.class [-h|-m|-t] file1.jpg file2.png file3.heif [...]");
			System.out.println("    -h : output results in semi-HTML");
			System.out.println("    -m : output results in Markdown");
			System.out.println("    -t : output results in plain text (default)");
			System.out.println("    --help : detailed description");
			return;
		}

		for (String s: args) {
			if (s.equals("-t")) {
				outputtype = OutputFormat.out_text;
				continue;
			}			
			if (s.equals("-h")) {
				outputtype = OutputFormat.out_html;
				continue;
			}	
			if (s.equals("-m")) {
				outputtype = OutputFormat.out_md;
				continue;
			}
			if (s.equals("--help")) {
				System.out.println("Image Location and Privacy Scanner v" + pluginVersion);
				for (String para : CommandLineHelp) {
					// by vitaut from https://stackoverflow.com/questions/4212675/wrap-the-string-after-a-number-of-characters-word-wise-in-java
					// this code does not work on multi-paragraph \n'd string, thus added the array hack. 
					StringBuilder sb = new StringBuilder(para);
					int i = 0;
					int width = 80;
					while (i + width < sb.length() && (i = sb.lastIndexOf(" ", i + width)) != -1) {
						sb.replace(i, i + 1, "\n");
					}
					sb.append("\n");
					System.out.println(sb.toString());
				}
				System.exit(0);
			}


			File f = new File(s);
			try (FileInputStream fis = new FileInputStream(f)) {
				switch (outputtype) {
					case out_text:
						System.out.print("Processing " + s + " : ");
						break;
					case out_html:
						System.out.println("<h3>" + s + "</h3>");
						break;
					case out_md:
						System.out.print("## " + s);
						break;
				}

				long size = f.length();
				byte[] data = new byte[(int) size];
				long read = fis.read(data);
				if (read != size) {
				    System.out.println("There was a problem reading the file");
				}
				// ZAP build fail using -Werror with msg:
				// `warning: [try] explicit call to close() on an auto-closeable resource`
				//fis.close();
				
				String res = scanForLocationInImage(data, outputtype);
				if (0 == res.length())  {
					System.out.println("None");
				} else {
					System.out.println("\n" + res);
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

// vim: autoindent noexpandtab tabstop=4 shiftwidth=4

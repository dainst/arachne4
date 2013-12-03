package de.uni_koeln.arachne.util;

/**
 * This class provides image mime type constants and a utility functions.
 */
public class ImageMimeUtil {
	
	public static final String BMP = "image/bmp";
	public static final String TIFF = "image/tiff";
	public static final String GIF = "image/gif";
	public static final String PNG = "image/png";
	public static final String JPEG = "image/jpeg";
	public static final String X_BITMAP = "image/x-bitmap";
	public static final String X_PIXMAP = "image/x-pixmap";
		
	/**
	 * Function to get the image type from the raw byte data. Only supported image types are checked for.
	 * @param imageData The byte data to check.
	 * @return The mime type of the image or null if the data could not be identified.
	 */
	@SuppressWarnings("PMD")
	public static String getImageType(final byte[] imageData)
	{
		if (imageData!=null) {

			final byte[] header = new byte[11];
			System.arraycopy(imageData, 0, header, 0, Math.min(imageData.length, header.length));
			final int header0 = header[0] & 0xff;
			final int header1 = header[1] & 0xff;
			final int header2 = header[2] & 0xff;
			final int header3 = header[3] & 0xff;
			final int header4 = header[4] & 0xff;
			final int header5 = header[5] & 0xff;
			final int header6 = header[6] & 0xff;
			final int header7 = header[7] & 0xff;
			final int header8 = header[8] & 0xff;
			final int header9 = header[9] & 0xff;
			final int header10 = header[10] & 0xff;

			if (header0 == 'B' && header1 == 'M') {
				return BMP;
			}
			
			
			if (header0 == 0x49 && header1 == 0x49 && header2 == 0x2a && header3 == 0x00) {
				return TIFF;
			}

			if (header0 == 0x4D && header1 == 0x4D && header2 == 0x00 && header3 == 0x2a) {
				return TIFF;
			}

			if (header0 == 'G' && header1 == 'I' && header2 == 'F' && header3 == '8') {
				return GIF;
			}
			if (header0 == 137 && header1 == 80 && header2 == 78 && header3 == 71 && header4 == 13 && header5 == 10 
					&& header6 == 26 && header7 == 10) {
				return PNG;
			}
			
			if (header0 == 0xFF && header1 == 0xD8 && header2 == 0xFF) {
				if (header3 == 0xE0 || header3 == 0xEE) {
					return JPEG;
				}

				// exif
				if ((header3 == 0xE1) && (header6 == 'E' && header7 == 'x' && header8 == 'i' && header9 == 'f' 
						&& header10 == 0)) {
					return JPEG;
				}
			}
			
			if (header0 == '#' && header1 == 'd' && header2 == 'e' && header3 == 'f') {
				return X_BITMAP;
			}

			if (header0 == '!' && header1 == ' ' && header2 == 'X' && header3 == 'P' && header4 == 'M' 
					&& header5 == '2') {
				return X_PIXMAP;
			}
		}
		return null;
	}
}

package de.uni_koeln.arachne.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.uni_koeln.arachne.response.Dataset;

/**
 * This service class manages the access to external image repositories
 * @author Sven Ole Clemens
 *
 */
@Service("aracheImageStreamService")
public class ImageStreamService {
	
	private static Logger logger = LoggerFactory.getLogger(ImageStreamService.class);
	
	/**
	 * ex: http://arachne.uni-koeln.de
	 */
	private String repositoryLink;
	
	@Autowired 
	private ServletContext servletContext;
	
	/**
	 * method to get a byte array representation of the image
	 * @param id 
	 * @return
	 * @throws IOException 
	 */
	// TODO: change to the new Image-Server
	public BufferedImage getArachneImage(ImageResolutionType imageResolutionType, Dataset imageEntity, String watermarkFilename) throws IOException {
		
		int width = imageResolutionType.width();
		int height = imageResolutionType.height();
		
		URL url = new URL(imageEntity.getField("marbilder.Pfad"));
		BufferedImage originalImage = ImageIO.read(url.openStream());
		
		// return original if no maximum size is specified
		if (width == 0) {
			if (watermarkFilename == null || watermarkFilename.equals("")) {
				return originalImage;
			}
			width = originalImage.getWidth();
			height = originalImage.getHeight();
		}
		
		int origWidth = originalImage.getWidth();
		int origHeight = originalImage.getHeight();
		float ratio = ((float) origWidth) / origHeight;
		
		if (ratio > 1) {
			height = Math.round(width / ratio);
		} else {
			width = Math.round(height * ratio);
		}
		
		Long startTime = System.currentTimeMillis();
		
		BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
		Graphics2D g = resizedImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawImage(originalImage, 0, 0, width, height, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);
		
		if (watermarkFilename != null && !watermarkFilename.isEmpty()) {
			InputStream watermark = servletContext.getResourceAsStream("/WEB-INF/watermarks/" + watermarkFilename);
			BufferedImage watermarkImage = ImageIO.read(watermark);
			g.drawImage(
					watermarkImage,
					width-watermarkImage.getWidth(), 0,
					width, watermarkImage.getHeight(),
					0, 0,
					watermarkImage.getWidth(), watermarkImage.getHeight(),
					null
				);
		}
		
		g.dispose();
		
		logger.debug("Time taken for image resizing: {} ms", System.currentTimeMillis() - startTime);
		
		return resizedImage;
		
	}

	/**
	 * @return the repositoryLink
	 */
	public String getRepositoryLink() {
		return repositoryLink;
	}

	/**
	 * @param repositoryLink the repositoryLink to set
	 */
	public void setRepositoryLink(String repositoryLink) {
		this.repositoryLink = repositoryLink;
	}
	
	

}

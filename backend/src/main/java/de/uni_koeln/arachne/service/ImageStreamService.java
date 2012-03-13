package de.uni_koeln.arachne.service;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

/**
 * This service class manages the access to external image repositories
 * @author Sven Ole Clemens
 *
 */
@Service("aracheImageStreamService")
public class ImageStreamService {
	
	/**
	 * ex: http://arachne.uni-koeln.de
	 */
	private String repositoryLink;
	
	/**
	 * method to get a byte array representation of the image
	 * @param id 
	 * @return
	 * @throws IOException 
	 */
	// TODO: change to the new Image-Server
	public BufferedImage getArachneImage(ImageResolutionType imageResolutionType, Long id) throws IOException {
		
		int width = imageResolutionType.width();
		int height = imageResolutionType.height();
		
		URL url = new URL(repositoryLink + "/arachne/images/image.php?key=" + id + "&method=min&width=" + width + "&height=" + height);
		BufferedImage bufferedImage = ImageIO.read(url.openStream());
		return bufferedImage;
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

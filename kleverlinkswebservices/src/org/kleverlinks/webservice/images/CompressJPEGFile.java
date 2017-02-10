package org.kleverlinks.webservice.images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kleverlinks.webservice.DataSourceConnection;

@Path("CompressFileService")
public class CompressJPEGFile {

    public static void main(String[] args) throws IOException {
    	CompressJPEGFile compressJPEGFile = new CompressJPEGFile();
    	compressJPEGFile.compressImage("Image.jpg");
    }
    @GET  
    @Path("/compressImage/{fileURL}")  
    @Produces(MediaType.TEXT_PLAIN)
    public String compressImage(@PathParam("fileURL") String fileURL) throws IOException{
    	File imageFile = new File(fileURL);
        File compressedImageFile = new File("compressed_"+fileURL);
        InputStream is = new FileInputStream(imageFile);
        OutputStream os = new FileOutputStream(compressedImageFile);
        float quality = 0.1f;
        // create a BufferedImage as the result of decoding the supplied InputStream
        BufferedImage image = ImageIO.read(is);
        // get all image writers for JPG format
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext())
            throw new IllegalStateException("No writers found");
        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os);
        writer.setOutput(ios);
        ImageWriteParam param = writer.getDefaultWriteParam();
        // compress to a given quality
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);
        // appends a complete image stream containing a single image and
        //associated stream and image metadata and thumbnails to the output
        writer.write(null, new IIOImage(image, null, null), param);
        // close all streams
        is.close();
        os.close();
        ios.close();
        writer.dispose();
        return "compressed_"+fileURL;
    }
    @GET  
    @Path("/uploadImage")  
    @Produces(MediaType.TEXT_PLAIN)
	public void uploadImage() {
		PreparedStatement psmt = null;
		try {
			
			 File image = new File("/home/thrymr/Downloads/1.jpg");
	         psmt = DataSourceConnection.getDBConnection().prepareStatement("insert into tbl_usertransactions(image) "+ "values(?)");
	         System.out.println("length===========b==========="+image.length());
	         FileInputStream fis = new FileInputStream(image);
	         psmt.setBinaryStream(1, (InputStream)fis, (int)(image.length()));
	         int s = psmt.executeUpdate();
	         psmt.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}

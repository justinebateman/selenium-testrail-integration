package business;

import sun.misc.BASE64Encoder;

import javax.activation.MimetypesFileTypeMap;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

public class Image
{
    public static String saveFileFromUrl(String imageUrl, String destinationFile) throws IOException
    {
        try
        {
            URL url = new URL(imageUrl);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(destinationFile);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1)
            {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return destinationFile;
    }

    public static String convertImageToBase64(BufferedImage image, String type)
    {
        String imageString = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try
        {
            ImageIO.write(image, type, bos);
            byte[] imageBytes = bos.toByteArray();

            BASE64Encoder encoder = new BASE64Encoder();
            imageString = encoder.encode(imageBytes);

            bos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return imageString;
    }

    public static Boolean isFileImage(String pFilePath)
    {
        Boolean isFileImage = false;
        File f = new File(pFilePath);
        String mimetype = new MimetypesFileTypeMap().getContentType(f);
        String type = mimetype.split("/")[0];
        if (type.equals("image"))
            isFileImage = true;
        return isFileImage;
    }

    public static Boolean isFileImageExtension(String pFilePath)
    {
        Boolean isFileImage = false;
        String path = pFilePath.toLowerCase();
        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg"))
        {
            isFileImage = true;
        }
        return isFileImage;
    }
}

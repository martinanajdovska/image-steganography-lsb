package is.imagesteganography.service;

import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public interface MessageService {
    byte[] getEncodedImageLSB1(MultipartFile multipartFile, String message) throws IOException;
    byte[] getEncodedImageLSB3(MultipartFile multipartFile, String message) throws IOException;
    byte[] getImageBytes(BufferedImage multipartFile) throws IOException;
    List<Byte> decodeBytesLSB1(MultipartFile multipartFile) throws IOException;
    List<Byte> decodeBytesLSB3(MultipartFile multipartFile) throws IOException;
    String getDecodedMessage(MultipartFile multipartFile, String group) throws IOException;
}

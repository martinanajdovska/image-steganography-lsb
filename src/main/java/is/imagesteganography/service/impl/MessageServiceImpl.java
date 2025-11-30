package is.imagesteganography.service.impl;

import is.imagesteganography.service.MessageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Override
    public byte[] getEncodedImageLSB1(MultipartFile multipartFile, String message) throws IOException {
        BufferedImage image = ImageIO.read(multipartFile.getInputStream());
        byte[] messageBytes = message.getBytes();

        if (image.getHeight() * image.getHeight() <= messageBytes.length) return null;
        int y = 0;
        int x = 0;
        int i = 0;

        int pixelMask = 0xF8; // get every bit except the 3 least significant bits
        int bitMask = 0x07;  // get the 3 least significant bits

        while (y < image.getHeight()) {
            while (x < image.getWidth() && i < messageBytes.length) {
                byte letterByte = messageBytes[i]; // get letter
                Color oldColor = new Color(image.getRGB(x, y));
                int red = oldColor.getRed(), green = oldColor.getGreen(), blue = oldColor.getBlue();

                red = red & pixelMask | letterByte & bitMask; // copy 3 bits of the letter into the red channel
                letterByte = (byte) (letterByte >> 3); // shift the copied bits

                green = green & 0xFC | letterByte & 0x03; // copy 2 bits instead of 3 for the green channel
                letterByte = (byte) (letterByte >> 2);

                blue = blue & pixelMask | letterByte & bitMask;

                Color newColor = new Color(red, green, blue);
                image.setRGB(x, y, newColor.getRGB()); // update the pixel with the encoded letter
                i++;
                x++;
            }
            if (i == messageBytes.length) break;
            y++;
            x = 0;
        }

        return getImageBytes(image);
    }

    public byte[] getEncodedImageLSB3(MultipartFile multipartFile, String message) throws IOException {
        BufferedImage image = ImageIO.read(multipartFile.getInputStream());
        byte[] messageBytes = message.getBytes();

        if (image.getHeight() * image.getHeight() <= messageBytes.length) return null;
        int y = 0;
        int x = 0;
        int i = 0;

        while (y < image.getHeight()) {
            while (x < image.getWidth() - 3 && i < messageBytes.length) {
                byte letterByte = messageBytes[i]; // get letter

                for (int pixel = 0; pixel < 3; pixel++) {
                    Color oldColor = new Color(image.getRGB(x, y));
                    int red = oldColor.getRed(), green = oldColor.getGreen(), blue = oldColor.getBlue();

                    // encode one letter bit into chosen channel then shift the bits
                    red = changeChannelValue(red,letterByte);
                    letterByte = (byte) (letterByte << 1);

                    green = changeChannelValue(green,letterByte);
                    letterByte = (byte) (letterByte << 1);

                    // when on the last iteration of the 3 pixels check if message is over
                    if (pixel == 2) {
                        if (i == messageBytes.length - 1) {
                            if (blue % 2 == 0) blue++; // if the end of the message, make the bit odd to flag the end
                        } else {
                            if (blue % 2 != 0) {
                                // if not the end of the message, make the bit even
                                // make sure channel values stay in range 0-255
                                if (blue == 255) blue--;
                                else blue++;
                            }
                        }
                    } else {
                        blue = changeChannelValue(blue,letterByte);
                        letterByte = (byte) (letterByte << 1);
                    }

                    Color newColor = new Color(red, green, blue);
                    image.setRGB(x, y, newColor.getRGB()); // update the pixel with the encoded letter

                    x++;
                }
                i++;
            }
            if (i == messageBytes.length) break;
            y++;
            x = 0;
        }

        return getImageBytes(image);
    }

    // match pixel and letter lsb on even or odd
    private int changeChannelValue(int channel, byte letter){
        // if letter bit is even and channel value is odd then make channel value even as well
        if ((letter & 0x80) != 0x80 && channel % 2 != 0) {
            channel--;
        } // if letter bit is odd and channel value is even then make channel value odd as well
        else if ((letter & 0x80) == 0x80 && channel % 2 == 0) {
            // check if channel value will stay in range 0-255
            if (channel != 0) {
                channel--;
            } else {
                channel++;
            }
        }
        return channel;
    }

    @Override
    public List<Byte> decodeBytesLSB1(MultipartFile multipartFile) throws IOException {
        BufferedImage image = ImageIO.read(multipartFile.getInputStream());

        int bits = 0;
        int pixelMask = 0x07;
        int y = 0;
        int x = 0;
        List<Byte> messageBytes = new ArrayList<>();

        while (y < image.getHeight()) {
            while (x < image.getWidth() && (byte) bits != (byte) '#') {
                Color color = new Color(image.getRGB(x, y));
                int red = color.getRed(), green = color.getGreen(), blue = color.getBlue();

                // copy the encoded bits from the color channels in reverse order
                bits = (blue & pixelMask);

                bits = bits << 2;
                bits = bits | (green & 0x03);

                bits = bits << 3;
                bits = bits | (red & pixelMask);

                messageBytes.add((byte) bits);
                x++;
            }
            if ((byte) bits == (byte) '#') break;
            y++;
            x = 0;
        }

        return (messageBytes.get(messageBytes.size() - 1) == (byte) '#') ? messageBytes : null;
    }

    public List<Byte> decodeBytesLSB3(MultipartFile multipartFile) throws IOException {
        BufferedImage image = ImageIO.read(multipartFile.getInputStream());

        int y = 0;
        int x = 0;
        List<Byte> messageBytes = new ArrayList<>();

        while (y < image.getHeight()) {
            while (x < image.getWidth() - 3) {
                int bits = 0;
                for (int pixel = 0; pixel < 3; pixel++) {
                    Color oldColor = new Color(image.getRGB(x, y));
                    int red = oldColor.getRed(), green = oldColor.getGreen(), blue = oldColor.getBlue();

                    // copy the least significant bit's value
                    bits = bits | (red % 2);

                    bits = bits << 1;
                    bits = bits | (green % 2);

                    if (pixel == 2) {
                        if (blue % 2 == 1) { // if the bit is odd, the message is finished
                            messageBytes.add((byte) bits);
                            return messageBytes;
                        } else {
                            x++;
                            messageBytes.add((byte) bits);
                            break;
                        }
                    } else {
                        bits = bits << 1;
                        bits = bits | (blue % 2);
                    }

                    bits = bits << 1;
                    x++;
                }
            }
            y++;
            x = 0;
        }
        return messageBytes;
    }

    @Override
    public String getDecodedMessage(MultipartFile multipartFile, String group) throws IOException {
        List<Byte> messageBytes;
        if (group.equals("lsb1")) {
            messageBytes = decodeBytesLSB1(multipartFile);
            if (messageBytes != null) messageBytes.remove(messageBytes.size() - 1); // remove end of message character #
        } else {
            messageBytes = decodeBytesLSB3(multipartFile);
        }
        if (messageBytes == null) return "There was no hidden message.";

        StringBuilder stringBuilder = new StringBuilder();
        for (Byte messageByte : messageBytes) {
            stringBuilder.append((char) (messageByte.intValue()));
        }
        return stringBuilder.toString();
    }

    @Override
    public byte[] getImageBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", byteArrayOutputStream);

        return byteArrayOutputStream.toByteArray();
    }
}

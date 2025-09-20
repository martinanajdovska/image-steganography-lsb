package is.imagesteganography.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import is.imagesteganography.service.MessageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/")
public class MessagesController {
    private final MessageService messageService;
    private byte[] encodedImage;

    public MessagesController(MessageService messageService) {
        this.messageService = messageService;
        this.encodedImage = null;
    }

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/encode")
    public String encode() {
        return "encode";
    }

    @GetMapping("/decode")
    public String decode() {
        return "decode";
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadEncodedImage() {

        if (this.encodedImage == null || this.encodedImage.length == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"encoded_image.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(this.encodedImage);
    }

    @PostMapping("/encode")
    public String encode(Model model, @RequestParam("image") MultipartFile multipartFile, @RequestParam String message, @RequestParam String group) throws IOException {
        if (group.equals("lsb1")) {
            this.encodedImage = this.messageService.getEncodedImageLSB1(multipartFile,message);
        } else {
            this.encodedImage = this.messageService.getEncodedImageLSB3(multipartFile, message);
        }
        if (this.encodedImage == null) model.addAttribute("imageError", true);
        model.addAttribute("encodedImage", encodedImage);
        return "index";
    }

    @PostMapping("/decode")
    public String decode(Model model, @RequestParam("image") MultipartFile multipartFile, @RequestParam String group) throws IOException {
        String message = this.messageService.getDecodedMessage(multipartFile, group);
        if (message.equals("There was no hidden message.")) model.addAttribute("error", true);
        model.addAttribute("decodedMessage", message);
        return "index";
    }
}

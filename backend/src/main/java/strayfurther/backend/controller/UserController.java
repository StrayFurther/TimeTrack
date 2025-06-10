package strayfurther.backend.controller;

import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import strayfurther.backend.dto.LoginRequestDTO;
import strayfurther.backend.service.ProfilePicService;
import strayfurther.backend.service.UserService;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.dto.UserRequestDTO;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;
    private ProfilePicService fileService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDTO userRequest) {
        userService.registerUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        String token = userService.loginUser(loginRequest);
        if (token != null) {
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Invalid credentials"));
        }
    }

    // Using request parameter for email check since depending on the frontend implementation, it might be easier to handle
    // and some frontends or libraries might do wrong email encoding
    @GetMapping("/exists")
    public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
        System.out.println("EEEEEEEMAIL: " + email);
        boolean exists = userService.emailExists(email.toLowerCase(Locale.ROOT));
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/profile-pic")
    public ResponseEntity<?> uploadProfilePic(@RequestParam("file") MultipartFile file) {
        if (file.getSize() > 2 * 1024 * 1024) {
            return ResponseEntity.badRequest().body("File too large");
        }
        if (!file.getContentType().startsWith("image/")) {
            return ResponseEntity.badRequest().body("Invalid file type");
        }
        try {
            String filePath = fileService.saveProfilePic(file);
            return ResponseEntity.ok(Collections.singletonMap("filePath", filePath));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file");
        }
    }

    @GetMapping("/profile-pic/{userId}")
    public ResponseEntity<?> getProfilePic(@PathVariable Long userId) {
        String filePath = fileService.getProfilePicPath(userId);
        try {
            Resource file = fileService.loadFileAsResource(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(file);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Profile picture not found");
        }
    }


}

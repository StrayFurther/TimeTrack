package strayfurther.backend.controller;

import org.springframework.core.io.Resource;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import strayfurther.backend.dto.LoginRequestDTO;
import strayfurther.backend.exception.FileStorageException;
import strayfurther.backend.service.UserService;

import java.util.Collections;
import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.dto.UserRequestDTO;
import strayfurther.backend.util.JwtUtil;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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
    public ResponseEntity<?> uploadProfilePic(@RequestHeader("Authorization") String authHeader, @RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest().body("File too large");
            }
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Invalid file type");
            }
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            String filePath = userService.uploadProfilePic(token, file);
            return ResponseEntity.ok(Collections.singletonMap("filePath", filePath));
        } catch (FileStorageException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/profile-pic")
    public ResponseEntity<?> getProfilePic(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            Resource file = userService.getUserProfilePic(token);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(file);
        } catch (FileStorageException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Profile picture not found");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid or missing token");
        }
    }

}

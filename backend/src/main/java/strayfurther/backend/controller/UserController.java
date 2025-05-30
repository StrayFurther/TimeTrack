package strayfurther.backend.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import strayfurther.backend.dto.LoginRequestDTO;
import strayfurther.backend.service.UserService;
import java.util.Collections;
import java.util.Locale;

import strayfurther.backend.dto.UserRequestDTO;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/user")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequestDTO userRequest) {
        System.out.println("HECKIN REGSTER USER: " + userRequest);
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
        return ResponseEntity.ok(Collections.singletonMap("value", exists));
    }



}

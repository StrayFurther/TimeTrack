package strayfurther.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.dto.LoginRequestDTO;
import strayfurther.backend.dto.UserRequestDTO;
import strayfurther.backend.exception.EmailAlreadyUsedException;
import strayfurther.backend.exception.FileStorageException;
import strayfurther.backend.exception.JwtUtilException;
import strayfurther.backend.exception.UserNotFoundException;
import strayfurther.backend.model.User;
import strayfurther.backend.repository.UserRepository;
import strayfurther.backend.util.JwtUtil;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ProfilePicService fileService;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, ProfilePicService fileService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
        this.fileService = fileService;
    }

    public void registerUser(UserRequestDTO userRequest) {
        String normalizedEmail = userRequest.getEmail().toLowerCase(Locale.ROOT);
        userRequest.setEmail(normalizedEmail);

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new EmailAlreadyUsedException("Email already in use");
        }
        User user = new User();
        user.setUserName(userRequest.getUserName());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        userRepository.save(user);
    }

    public String loginUser(LoginRequestDTO loginRequest) {
        String normalizedEmail = loginRequest.getEmail().toLowerCase(Locale.ROOT);
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), userOptional.get().getPassword())) {
            return null;
        }
        return jwtUtil.generateToken(userOptional.get().getEmail());

    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase(Locale.ROOT));
    }

    public User getUserFromToken(String token) throws UserNotFoundException, JwtUtilException {
        try {
            String email = jwtUtil.extractEmail(token);
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        } catch (JwtUtilException e) {
            throw new RuntimeException("Invalid token", e);
        } catch (Exception e) {
            throw new UserNotFoundException("User not found or invalid token: " + e.getMessage(), e);
        }
    }

    public Resource getUserProfilePic(String token) throws FileStorageException {
        User user = getUserFromToken(token);
        return fileService.loadFileAsResource(user.getProfilePic());
    }

    public String uploadProfilePic(String token, MultipartFile file) throws FileStorageException {
        User user = getUserFromToken(token);

        if (user.getProfilePic() != null) {
            String oldFileName = user.getProfilePic();
            if (!fileService.deletePic(oldFileName)) {
                throw new FileStorageException("Failed to delete old profile picture: " + oldFileName);
            }
        }

        String fileName = fileService.saveProfilePic(file);
        user.setProfilePic(fileName);
        userRepository.save(user);
        return fileName;
    }

}
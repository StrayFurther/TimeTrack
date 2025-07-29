package strayfurther.backend.service;

import org.springframework.core.io.Resource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import strayfurther.backend.dto.*;
import strayfurther.backend.exception.EmailAlreadyUsedException;
import strayfurther.backend.exception.FileStorageException;
import strayfurther.backend.exception.JwtUtilException;
import strayfurther.backend.exception.UserNotFoundException;
import strayfurther.backend.model.User;
import strayfurther.backend.repository.UserRepository;
import strayfurther.backend.util.JwtUtil;
import strayfurther.backend.dto.UpdateUserDTO;
import strayfurther.backend.dto.AdminsUpdateUserDTO;


import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ProfilePicService fileService;
    private final ProfilePicService profilePicService;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil, ProfilePicService fileService, ProfilePicService profilePicService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
        this.fileService = fileService;
        this.profilePicService = profilePicService;
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

    public String loginUser(LoginRequestDTO loginRequest, String userAgent) {
        String normalizedEmail = loginRequest.getEmail().toLowerCase(Locale.ROOT);
        Optional<User> userOptional = userRepository.findByEmail(normalizedEmail);
        if (userOptional.isEmpty() || !passwordEncoder.matches(loginRequest.getPassword(), userOptional.get().getPassword())) {
            return null;
        }

        return jwtUtil.generateToken(userOptional.get().getEmail(), userAgent);
    }

    public boolean emailExists(String email) {
        System.out.println("Checking if email exists in service: " + email);
        return userRepository.existsByEmail(email.toLowerCase(Locale.ROOT));
    }

    public User getUserFromToken(String token) throws UserNotFoundException, JwtUtilException {
        try {
            String email = jwtUtil.extractJWTDetails(token).getEmail();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        } catch (JwtUtilException e) {
            throw new RuntimeException("Invalid token", e);
        } catch (Exception e) {
            throw new UserNotFoundException("User not found or invalid token: " + e.getMessage(), e);
        }
    }

    public UserDetailDTO getUserDetailsFromToken(String token) throws UsernameNotFoundException {
        User user = getUserFromToken(token);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        return UserDetailDTO.builder()
                .userName(user.getUserName())
                .role(user.getRole())
                .email(user.getEmail()).build();
    }



    public Resource getUserProfilePic(String token) throws FileStorageException {
        User user = getUserFromToken(token);
        return fileService.loadFileAsResource(user.getProfilePic());
    }

    public String uploadProfilePic(String token, MultipartFile file) throws FileStorageException {
        User user = getUserFromToken(token);

        if (user.getProfilePic() != null) {
            String oldFileName = user.getProfilePic();
            try {
                if (profilePicService.isFileSaved(oldFileName)) {
                    if (!fileService.deletePic(oldFileName)) {
                        throw new FileStorageException("Failed to delete old profile picture: " + oldFileName);
                    }
                }
            } catch (FileStorageException e) {
                throw new FileStorageException("Failed to delete old profile picture: " + oldFileName, e);
            }
        }

        String fileName = fileService.saveProfilePic(file);
        user.setProfilePic(fileName);
        userRepository.save(user);
        return fileName;
    }

    public User updateUserDetails(String token, UpdateUserDTO userUpdateDTO) throws UserNotFoundException, JwtUtilException {
        User userToUpdate = getUserFromToken(token);
        if (userToUpdate == null) {
            throw new UserNotFoundException("User not found");
        }

        userToUpdate.setUserName(userUpdateDTO.getUserName());
        if (userUpdateDTO.getPassword() != null) {
            userToUpdate.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }
        User updatedUser = userRepository.save(userToUpdate);
        if (updatedUser == null) {
            throw new UserNotFoundException("Failed to update user details");
        }
        return updatedUser;
    }

    public User updateUserDetailsAsAdmin(Long id, AdminsUpdateUserDTO userUpdateDTO) throws UserNotFoundException, JwtUtilException {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        User userToUpdate = user.get();

        userToUpdate.setUserName(userUpdateDTO.getUserName());
        if (userUpdateDTO.getPassword() != null) {
            userToUpdate.setPassword(passwordEncoder.encode(userUpdateDTO.getPassword()));
        }
        userToUpdate.setRole(userUpdateDTO.getRole());
        User updatedUser = userRepository.save(userToUpdate);
        if (updatedUser == null) {
            throw new UserNotFoundException("Failed to update user details");
        }
        return updatedUser;
    }

    //TODO: new method to update email adresses with confirmation mails send to new mail address

}
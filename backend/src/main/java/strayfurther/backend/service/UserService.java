package strayfurther.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import strayfurther.backend.dto.LoginRequestDTO;
import strayfurther.backend.dto.UserRequestDTO;
import strayfurther.backend.exception.EmailAlreadyUsedException;
import strayfurther.backend.model.User;
import strayfurther.backend.repository.UserRepository;
import strayfurther.backend.util.JwtUtil;

import java.util.Locale;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
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
        return JwtUtil.generateToken(userOptional.get().getEmail());

    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.toLowerCase(Locale.ROOT));
    }
}
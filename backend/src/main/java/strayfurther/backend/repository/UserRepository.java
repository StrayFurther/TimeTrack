package strayfurther.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import strayfurther.backend.model.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Custom queries can go here (e.g. findByName)
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}

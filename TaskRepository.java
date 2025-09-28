package main.java.com.example.taskapi.repository;

import com.example.taskapi.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByNameContainingIgnoreCase(String name);

    List<Task> findAll();

    Optional<Task> findById(String id);

    Task save(Task t);

    void deleteById(String id);
}

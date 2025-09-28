package main.java.com.example.taskapi.service;

import com.example.taskapi.model.Task;
import main.java.com.example.taskapi.model.TaskExecution;
import main.java.com.example.taskapi.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@service
public class TaskService {

    @Autowired
    private TaskRepository taskRepo;

    // List of blocked commands for safety (requires Java 9+ for List.of)
    private static final List<String> BLOCKED = java.util.List.of(
        "rm", "shutdown", "reboot", "mkfs", "dd", ":(){", ">|", "fork"
    );

    public boolean isSafeCommand(String cmd) {
        if (cmd == null || cmd.isEmpty()) {
            return false;
        }
        return BLOCKED.stream().noneMatch(cmd::contains);
    }

    public List<Task> getAllTasks() {
        return taskRepo.findAll();
    }

    public Optional<Task> getTask(String id) {
        return taskRepo.findById(id);
    }

    public Task saveTask(Task t) {
        return taskRepo.save(t);
    }

    public void deleteTask(String id) {
        taskRepo.deleteById(id);
    }

    public List<Task> findByName(String name) {
        return taskRepo.findByNameContainingIgnoreCase(name);
    }

    /**
     * @param t
     * @return
     * @throws Exception
     */
    public TaskExecution runTaskAndRecord(Task t) throws Exception {
        if (!isSafeCommand(t.getCommand())) {
            throw new IllegalArgumentException("Unsafe command detected");
        }

        Date start = new Date();

        ProcessBuilder pb = new ProcessBuilder("bash", "-c", t.getCommand());
        pb.redirectErrorStream(true);

        StringBuilder output = new StringBuilder();

        Process p = pb.start();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        p.waitFor();
        Date end = new Date();

        TaskExecution exec = new TaskExecution(start, end, output.toString().trim());

        if (t.getTaskExecutions() == null) {
            t.setTaskExecutions(new ArrayList<>());
        }
        t.getTaskExecutions().add(exec);

        taskRepo.save(t);
        return exec;
    }
}

package main.java.com.example.taskapi.controller;

import com.example.taskapi.model.Task;
import com.example.taskapi.model.TaskExecution;
import com.example.taskapi.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<?> getTasks(@RequestParam(required = false) String id) {
        if (id == null) return ResponseEntity.ok(taskService.getAllTasks());
        return taskService.getTask(id)
                .map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping
    public ResponseEntity<?> createOrUpdate(@RequestBody Task t) {
        if (t.getCommand() == null || !taskService.isSafeCommand(t.getCommand()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsafe command detected");
        return ResponseEntity.ok(taskService.saveTask(t));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam String id) {
        if (!taskService.getTask(id).isPresent())
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        taskService.deleteTask(id);
        return ResponseEntity.ok("Deleted");
    }

    @GetMapping("/find")
    public ResponseEntity<?> findByName(@RequestParam String name) {
        List<Task> results = taskService.findByName(name);
        return results.isEmpty() ? new ResponseEntity<>(HttpStatus.NOT_FOUND) : ResponseEntity.ok(results);
    }

    @PutMapping("/{id}/execute")
    public ResponseEntity<?> execute(@PathVariable String id) {
        Optional<Task> taskOpt = taskService.getTask(id);
        if (!taskOpt.isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        try {
            var exec = taskService.runTaskAndRecord(taskOpt.get());
            return ResponseEntity.ok(exec);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Execution error: " + e);
        }
    }
}

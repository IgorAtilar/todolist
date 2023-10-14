package com.example.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity<?> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var userId = (UUID) request.getAttribute("userId");
        taskModel.setUserId(userId);

        var currentDate = LocalDateTime.now();

        if (currentDate.isAfter(taskModel.getStartAt())
                || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.badRequest()
                    .body("Start and end date must be greater than current date");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.badRequest().body("Start date must be less than end date");
        }

        var task = this.taskRepository.save(taskModel);

        return ResponseEntity.ok(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var userId = (UUID) request.getAttribute("userId");
        var tasks = this.taskRepository.findByUserId(userId);
        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@RequestBody TaskModel taskModel, @PathVariable UUID id,
            HttpServletRequest request) {

        var task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.badRequest().body("Task not found");
        }

        var userId = (UUID) request.getAttribute("userId");

        if (!task.getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body("You are not allowed to update this task");
        }

        Utils.copyNonNullProperties(taskModel, task);

        var updatedTask = this.taskRepository.save(task);
        return ResponseEntity.ok(updatedTask);
    }

}

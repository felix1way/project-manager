package com.exam.dao;

import com.exam.model.Task;
import java.util.List;

public interface ITaskDAO {
    List<Task> getAllTasks();
    void insert(Task t);
    void update(Task t);
    void delete(int id);
}

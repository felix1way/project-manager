package com.exam.dao;

import com.exam.model.Task;
import java.util.List;

public interface ITaskDAO {
    List<Task> getAllTasks();

    List<Task> search(String keyword);

    List<Task> sortByPriority(boolean asc);

    void insert(Task task);

    void update(Task task);

    void delete(int id);
}


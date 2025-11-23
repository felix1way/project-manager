package com.exam.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Task {
    private int id;
    private String title;
    private String description;
    private int priority;

    private List<Tag> tags = new ArrayList<>();

    public Task() {}

    public Task(int id, String title, String description, int priority) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public Task(String title, String description, int priority) {
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    public int getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public int getPriority() {
        return priority;
    }

    public void setId(int id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<Tag> getTags() {
        return tags;
    }
    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public String tagsAsString() {
        return tags.stream().map(Tag::getName).collect(Collectors.joining(", "));
    }
}

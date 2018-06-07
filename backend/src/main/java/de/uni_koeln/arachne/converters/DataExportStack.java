package de.uni_koeln.arachne.converters;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

@Service
public class DataExportStack {

    private Stack<DataExportTask> stack = new Stack<DataExportTask>();
    private ArrayList<DataExportTask> running = new ArrayList<DataExportTask>();
    private HashMap<String, DataExportTask> finished = new HashMap<String, DataExportTask>();

    public Integer MAX_STACK_SIZE = 10;
    public Integer MAX_THREADS = 1;

    public void push(DataExportTask task) {

        System.out.println("Push task " + task.name);
        System.out.println(stack.size() + " tasks in stack");

        if (stack.size() >= MAX_STACK_SIZE) {
            throw new DataExportException("stack_full", HttpStatus.SERVICE_UNAVAILABLE, "DE"); // @TODO correct language
        }

        if(running.size() >= MAX_THREADS) {
            stack.add(task);
            System.out.println("added task " + task.name + " to stack (" + stack.size() + ")");
        } else {
            runTask(task);
        }

    }

    public void runTask(DataExportTask task) {
        System.out.println("Run task: " + task.name);
        running.add(task);
        startThread(task);
    }

    public void nextTask() {
        System.out.println("Next task");
        try {
            runTask(stack.pop());
        } catch (EmptyStackException exception) {
            System.out.println("All tasks finished");
        }

    }

    private DataExportThread startThread(DataExportTask task) {
        final DataExportThread runnable = new DataExportThread(task);
        runnable.registerListener(this);
        final Thread t = new Thread(runnable);
        t.setUncaughtExceptionHandler((th, ex) -> System.out.println("Uncaught exception: " + ex));
        t.start();
        return runnable;
    }

    public void taskIsFinishedListener(DataExportTask task) {
        running.remove(task);
        finished.put(task.uuid.toString(), task);
        System.out.println("Finshed task:" + task.name);
        System.out.println(running.size() + " tasks in stack");
        nextTask();
    }

    public DataExportTask getFinishedTaskById(String taskId) {
        if (!finished.containsKey(taskId)) {
            throw new DataExportException("task_not_found", HttpStatus.NOT_FOUND, "DE"); // TODO language
        }
        return finished.get(taskId);
    }

    public Integer getTasksRunning() {
        return stack.size();
    }

    public JSONObject getStatus() {
        final JSONObject status = new JSONObject();
        status.put("max_stack_size", MAX_STACK_SIZE);
        status.put("max_threads", MAX_THREADS);
        status.put("tasks_running", running.size());
        status.put("tasks_enqueued", stack.size());
        final JSONObject taskList = new JSONObject();
        for (DataExportTask task: stack) {
            final JSONObject taskListItem = new JSONObject();
            taskListItem.put("name", task.name);
            taskListItem.put("status", "enqueued");
            taskList.put(task.uuid.toString(), taskListItem);
        }
        for (DataExportTask task: running) {
            final JSONObject taskListItem = new JSONObject();
            taskListItem.put("name", task.name);
            taskListItem.put("status", "running");
            taskList.put(task.uuid.toString(), taskListItem);
        }
        for (DataExportTask task: finished) {//! TODO
            final JSONObject taskListItem = new JSONObject();
            taskListItem.put("name", task.name);
            taskListItem.put("status", "finished");
            taskList.put(task.uuid.toString(), taskListItem);
        }
        status.put("tasks", taskList);
        return status;
    }
}

package de.uni_koeln.arachne.converters;

import de.uni_koeln.arachne.service.UserRightsService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;


@Service
public class DataExportStack {

    @Autowired
    private transient UserRightsService userRightsService;

    private Stack<DataExportTask> stack = new Stack<DataExportTask>();
    private ArrayList<DataExportTask> running = new ArrayList<DataExportTask>();
    private HashMap<String, DataExportTask> finished = new HashMap<String, DataExportTask>();

    @Value("${dataExportMaxStackSize:10}")
    private Integer dataExportMaxStackSize;

    @Value("${dataExportMaxThreads:4}")
    private Integer dataExportMaxThreads;

    public DataExportTask newTask(AbstractDataExportConverter converter,
                          DataExportConversionObject conversionObject) {
        DataExportTask task = new DataExportTask(converter, conversionObject);
        task.setOwner(userRightsService.getCurrentUser());
        task.setUrl(getRequestUrl());
        return task;
    }

    public void push(DataExportTask task) {

        if (!userRightsService.isSignedInUser()) {
            System.out.println("Not logged in");
            throw new DataExportException("too_huge_and_not_logged_in", HttpStatus.UNAUTHORIZED, "DE"); // TODO correct language
        }

        System.out.println("Push task " + task.uuid.toString());
        System.out.println(stack.size() + " tasks in stack");

        if (stack.size() >= dataExportMaxStackSize) {
            throw new DataExportException("stack_full", HttpStatus.SERVICE_UNAVAILABLE, "DE"); // @TODO correct language
        }

        if(running.size() >= dataExportMaxThreads) {
            stack.add(task);
            System.out.println("added task " + task.uuid.toString() + " to stack (" + stack.size() + ")");
        } else {
            runTask(task);
        }

    }

    public void runTask(DataExportTask task) {
        System.out.println("Run task: " + task.uuid.toString());
        task.startTimer();
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

    public void removeFinishedTask(DataExportTask task) {
        finished.remove(task.uuid.toString());
    }

    private DataExportThread startThread(DataExportTask task) {
        final DataExportThread dataExportThread = new DataExportThread(task, RequestContextHolder.currentRequestAttributes());

        dataExportThread.registerListener(this);
        final Thread thread = new Thread(dataExportThread);
        thread.setUncaughtExceptionHandler((t, e) -> {
            task.error = true;
            System.out.println("Error in task:" + task.uuid.toString() + ": " + e);
            e.printStackTrace();
        });
        thread.start();
        return dataExportThread;
    }

    public void taskIsFinishedListener(DataExportTask task) {
        task.stopTimer();
        running.remove(task);
        finished.put(task.uuid.toString(), task);
        System.out.println("Finished task:" + task.uuid.toString() + " " + running.size() + " tasks in stack");
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
        status.put("max_stack_size", dataExportMaxStackSize);
        status.put("max_threads", dataExportMaxThreads);
        status.put("tasks_running", running.size());
        status.put("tasks_enqueued", stack.size());
        final JSONObject taskList = new JSONObject();
        for (DataExportTask task: stack) {
            final JSONObject info = task.getInfoAsJSON();
            info.put("status", "enqueued");
            taskList.put(task.uuid.toString(), info);
        }
        for (DataExportTask task: running) {
            final JSONObject info = task.getInfoAsJSON();
            info.put("status", "running");
            taskList.put(task.uuid.toString(), info);
        }
        for (HashMap.Entry<String, DataExportTask> taskItem: finished.entrySet()) {
            final JSONObject info = taskItem.getValue().getInfoAsJSON();
            info.put("status", taskItem.getValue().error ? "error" : "finished");
            taskList.put(taskItem.getValue().uuid.toString(), info);
        }
        status.put("tasks", taskList);
        return status;
    }

    private String getRequestUrl() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sra.getRequest();
        return request.getRequestURL().toString() + "?" + request.getQueryString();
    }


}

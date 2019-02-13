package de.uni_koeln.arachne.export;

import de.uni_koeln.arachne.mapping.hibernate.User;
import de.uni_koeln.arachne.service.MailService;
import de.uni_koeln.arachne.service.Transl8Service;
import de.uni_koeln.arachne.service.UserRightsService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Stack;

/**
 * @author Paf
 */

@Service
public class DataExportStack {

    @Autowired
    private transient UserRightsService userRightsService;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private transient MailService mailService;

    @Autowired
    private DataExportFileManager dataExportFileManager;

    @Autowired
    public transient Transl8Service transl8Service;

    private static final Logger LOGGER = LoggerFactory.getLogger("DataExportLogger");

    private Stack<DataExportTask> stack = new Stack<DataExportTask>();
    private HashMap<String, DataExportTask> running = new HashMap<String, DataExportTask>();
    private HashMap<String, DataExportTask> finished = new HashMap<String, DataExportTask>();

    private String serverAddress;
    private Integer dataExportMaxStackSize;
    private Integer dataExportMaxThreads;
    private Integer dataExportMaxTaskLifeTime;

    public DataExportStack(
            final @Value("${dataExportMaxStackSize:10}") Integer maxStackSize,
            final @Value("${dataExportMaxThreads:4}") Integer maxThreads,
            final @Value("${dataExportMaxTaskLifeTime:86400000}") Integer maxLifeTime,
            final @Value("${serverAddress}") String serverAddress
    ) {
        dataExportMaxStackSize = maxStackSize;
        dataExportMaxThreads = maxThreads;
        dataExportMaxTaskLifeTime = maxLifeTime;
        this.serverAddress = serverAddress;
    }

    public DataExportTask newTask(AbstractDataExportConverter<?> converter, DataExportConversionObject conversionObject) {

        DataExportTask task = new DataExportTask(converter, conversionObject);
        task.setOwner(userRightsService.getCurrentUser());
        task.setRequestUrl(getRequestUrl());
        task.setBackendUrl(getBackendUrl());
        task.setUserRightsService(userRightsService);
        task.setLanguage(getRequestLanguage());

        return task;
    }

    public void push(DataExportTask task) {

        if (!userRightsService.isSignedInUser()) {
            LOGGER.info("Not logged in");
            throw new DataExportException("to_huge_and_not_logged_in", HttpStatus.UNAUTHORIZED);
        }

        LOGGER.info("Push task " + task.uuid.toString());
        LOGGER.info(stack.size() + " tasks in stack");

        if (stack.size() >= dataExportMaxStackSize) {
            throw new DataExportException("stack_full", HttpStatus.SERVICE_UNAVAILABLE);
        }

        if(running.size() >= dataExportMaxThreads) {
            stack.add(task);
            LOGGER.info("added task " + task.uuid.toString() + " to stack (" + stack.size() + ")");
        } else {
            runTask(task);
        }

    }

    public void runTask(DataExportTask task) {
        LOGGER.info("Run task: " + task.uuid.toString());
        task.startTimer();
        running.put(task.uuid.toString(), task);
        startThread(task);
    }

    public void nextTask() {
        LOGGER.info("Next task");
        try {
            runTask(stack.pop());
        } catch (EmptyStackException exception) {
            LOGGER.info("All tasks finished");

        }

    }

    public void dequeueTask(DataExportTask task) {
        if (!stack.contains(task)) {
            throw new DataExportException("task_not_found", HttpStatus.NOT_FOUND);
        }
        stack.remove(task);
        task.error = "aborted";
        finished.put(task.uuid.toString(), task);
        LOGGER.info("Task " + task.uuid.toString() + " dequeued");
    }

    public void abortTask(DataExportTask task) {
        if (!running.containsKey(task.uuid.toString())) {
            throw new DataExportException("task_not_found", HttpStatus.NOT_FOUND);
        }
        task.cancel();
        // moving to finished is done by taskIsFinishedListener
        dataExportFileManager.deleteFile(task);
    }

    public void removeFinishedTask(DataExportTask task) {
        if (!finished.containsKey(task.uuid.toString())) {
            throw new DataExportException("task_not_found", HttpStatus.NOT_FOUND);
        }
        finished.remove(task.uuid.toString());
        LOGGER.info("Task " + task.uuid.toString() + " removed");
    }

    private void startThread(DataExportTask task) {
        final DataExportThread dataExportThread = new DataExportThread(task, getRequest());
        taskExecutor.execute(dataExportThread);
        dataExportThread.setFileManager(dataExportFileManager);
        dataExportThread.registerListener(this);
    }

    public void taskIsFinishedListener(DataExportTask task) {
        task.stopTimer();
        running.remove(task.uuid.toString());
        finished.put(task.uuid.toString(), task);
        if (task.error == null) {
            String subject = "Arachne Data Export";
            String text = "%USER% | %URL% | %NAME%";
            final String user = (!task.getOwner().getLastname().equals("") && (task.getOwner().getLastname() != null))
                    ? task.getOwner().getFirstname() + " " + task.getOwner().getLastname()
                    : task.getOwner().getUsername();
            final String url = dataExportFileManager.getFileUrl(task);
            final String mail = task.getOwner().getEmail();
            try {
                subject = transl8Service.transl8("data_export_ready", task.getLanguage());
                text = transl8Service.transl8("data_export_success_mail", task.getLanguage());
            } catch (Transl8Service.Transl8Exception e) {
                // transl8 is not available.. normal, we don't panic...
            }
            text = text
                    .replace("%URL%", url)
                    .replace("%USER%", user)
                    .replace("%NAME%", task.getConversionName());

            mailService.sendMailHtml(mail, null, subject, text);
        }

        LOGGER.info("Finished task:" + task.uuid.toString() + " " + running.size() + " tasks in stack");
        nextTask();
    }

    public DataExportTask getEnqueuedTaskById(String taskId) {
        for (DataExportTask task: stack) {
            if (task.uuid.toString().equals(taskId)) {
                return task;
            }
        }
        return null;
    }

    public DataExportTask getRunningTaskById(String taskId) {
        if (!running.containsKey(taskId)) {
            return null;
        }
        return running.get(taskId);
    }

    public DataExportTask getFinishedTaskById(String taskId) {
        if (!finished.containsKey(taskId)) {
            return null;
        }
        return finished.get(taskId);
    }

    public JSONObject getStatus(User owner) {
        final JSONObject status = new JSONObject();
        status.put("max_stack_size", dataExportMaxStackSize);
        status.put("max_threads", dataExportMaxThreads);
        status.put("tasks_running", running.size());
        status.put("tasks_enqueued", stack.size());
        final JSONObject taskList = new JSONObject();

        for (DataExportTask task: getEnqueuedTasks(owner)) {
            final JSONObject info = task.getInfoAsJSON();
            info.put("status", "enqueued");
            taskList.put(task.uuid.toString(), info);
        }
        for (DataExportTask task: getRunningTasks(owner)) {
            final JSONObject info = task.getInfoAsJSON();
            info.put("status", "running");
            taskList.put(task.uuid.toString(), info);
        }
        for (DataExportTask task: getFinishedTasks(owner, false)) {
            final JSONObject info = task.getInfoAsJSON();
            info.put("status", (task.error != null) ? task.error : "finished");
            taskList.put(task.uuid.toString(), info);
        }
        status.put("tasks", taskList);
        return status;
    }

    public ArrayList<DataExportTask> getEnqueuedTasks(User owner) {
        final ArrayList<DataExportTask> taskList = new ArrayList<DataExportTask>();
        for (DataExportTask task: stack) {
            if (isTaskOwnedBy(owner, task)) {
                taskList.add(task);
            }
        }
        return taskList;
    }

    public ArrayList<DataExportTask> getRunningTasks(User owner) {
        final ArrayList<DataExportTask> taskList = new ArrayList<DataExportTask>();
        for (HashMap.Entry<String, DataExportTask> taskItem: running.entrySet()) {
            if (isTaskOwnedBy(owner, taskItem.getValue())) {
                taskList.add(taskItem.getValue());
            }
        }
        return taskList;
    }

    public ArrayList<DataExportTask> getFinishedTasks(User owner, Boolean outdated) {
        final ArrayList<DataExportTask> taskList = new ArrayList<DataExportTask>();
        for (HashMap.Entry<String, DataExportTask> taskItem: finished.entrySet()) {
            if (isTaskOwnedBy(owner, taskItem.getValue())) {
                if (!outdated || isTaskOutdated(taskItem.getValue())) {
                    taskList.add(taskItem.getValue());
                }
            }
        }
        return taskList;
    }

    private boolean isTaskOwnedBy(User owner, DataExportTask task) {
        return owner == null || (owner.getId() == task.getOwner().getId());
    }

    private boolean isTaskOutdated(DataExportTask task) {
        return (task.getAge() > dataExportMaxTaskLifeTime) || (task.getAge() < 0);
    }

    private String getRequestLanguage() {
        final String langParameter = getRequest().getParameter("lang");
        final String langHeader = getRequest().getHeader("Accept-Language");
        return (langParameter == null) ? ((langHeader == null) ? "de" : langHeader) : langParameter;
    }

    private String getRequestUrl() {
        return getRequest().getRequestURL().toString() + "?" + getRequest().getQueryString();
    }

    private String getBackendUrl() {
        return "https://" + serverAddress;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return sra.getRequest();
    }



}
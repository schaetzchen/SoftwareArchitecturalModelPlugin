package plugindata;

import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import plugindata.utils.component_identification.ComponentDataHandler;

public class MyProjComponent implements ProjectComponent {

    private static Project project;
    private static ApplicationConfiguration config;
    final private static String argsLine = "-javaagent:aspectjweaver.jar ";
    private static MessageBusConnection addParams, removeParams;

    public MyProjComponent(@NotNull Project project) {

        this.project = project;
        config = (ApplicationConfiguration) RunManager.getInstance(project).getSelectedConfiguration().getConfiguration();

        attachExecutionListeners();
    }

    private void attachExecutionListeners() {
//
//        addParams = project.getMessageBus().connect();
//        addParams.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
//            @Override
//            public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env) {
//
//                if (config.getVMParameters() == null)
//                    config.setVMParameters(argsLine);
//                else
//                    config.setVMParameters(argsLine + config.getVMParameters());
//            }
//        });

        project.getMessageBus().connect().subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@NotNull Project project) {
                ComponentDataHandler.retrievePackageDataInfo(project);
            }
        });
//
//        removeParams = project.getMessageBus().connect();
//        removeParams.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
//            @Override
//            public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {
//
//                String newparams = config.getVMParameters().substring(argsLine.length());
//                config.setVMParameters(newparams);
//            }
//        });
    }

    public static void connectParamsListeners() {

        addParams = project.getMessageBus().connect();
        addParams.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
            @Override
            public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env) {

                if (config.getVMParameters() == null)
                    config.setVMParameters(argsLine);
                else
                    config.setVMParameters(argsLine + config.getVMParameters());
            }
        });

        removeParams = project.getMessageBus().connect();
        removeParams.subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
            @Override
            public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {

                String newparams = config.getVMParameters().substring(argsLine.length());
                config.setVMParameters(newparams);
            }
        });
    }

    public static void disconnectParamsListeners() {
        if (addParams != null)
            addParams.disconnect();
        if (removeParams != null)
            removeParams.disconnect();
    }
}
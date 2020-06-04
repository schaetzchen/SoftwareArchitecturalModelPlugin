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
import org.jetbrains.annotations.NotNull;
import plugindata.utils.component_identification.ComponentDataHandler;

public class MyProjComponent implements ProjectComponent {

    private Project project;
    ApplicationConfiguration config;
    final String argsLine = "-javaagent:aspectjweaver.jar ";

    public MyProjComponent(@NotNull Project project) {

        this.project = project;
        config = (ApplicationConfiguration) RunManager.getInstance(project).getSelectedConfiguration().getConfiguration();

        attachExecutionListeners();
    }

    private void attachExecutionListeners() {

        project.getMessageBus().connect().subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
            @Override
            public void processStarting(@NotNull String executorId, @NotNull ExecutionEnvironment env) {

                if(config.getVMParameters() == null)
                    config.setVMParameters(argsLine);
                else
                    config.setVMParameters(argsLine + config.getVMParameters());
            }
        });

        project.getMessageBus().connect().subscribe(ProjectManager.TOPIC, new ProjectManagerListener() {
            @Override
            public void projectOpened(@NotNull Project project) {
                ComponentDataHandler.retrievePackageDataInfo(project);
            }
        });

        project.getMessageBus().connect().subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
            @Override
            public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {

                String newparams = config.getVMParameters().substring(argsLine.length());
                config.setVMParameters(newparams);
            }
        });
    }

//    private void addAJWeaverJarFile() {
//
//        File ajw = new File(getClass().getClassLoader().getResource("aspectjweaver.jar").getFile());
//        VirtualFile f = LocalFileSystem.getInstance().findFileByIoFile(ajw);
//        VirtualFile dir = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath()));
//
//        if (! new File(dir.getPath() + "\\aspectjweaver.jar").exists())
//            plugindata.RunnableHelper.runWriteCommand(project, new Runnable() {
//                @Override
//                public void run() {
//                        plugindata.RunnableHelper.handleFileCopy(this, f, dir);
//                }
//            });
//    }
//
//    private void addLoggingAspectFile() {
//
//        File aspect = new File(getClass().getClassLoader().getResource("LoggingAspect.java").getFile());
//        VirtualFile f = LocalFileSystem.getInstance().findFileByIoFile(aspect);
//        VirtualFile dir = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath() + "/src/executiondatalogging"));
//
//        if (! new File(dir.getPath() + "\\LoggingAspect.java").exists())
//            plugindata.RunnableHelper.runWriteCommand(project, new Runnable() {
//                @Override
//                public void run() {
//                    plugindata.RunnableHelper.handleFileCopy(this, f, dir);
//                }
//            });
//    }

//    private void changeProjectCompiler() {
//
//        JavaCompilersTab.
//
//        CompilerManager.getInstance(project).getCompilers()
//    }

    @Override
    public void initComponent() {

//        project.getMessageBus().connect().subscribe(ExecutionManager.EXECUTION_TOPIC, new ExecutionListener() {
//            @Override
//            public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
//                Messages.showMessageDialog("Code running!", "msg", Messages.getInformationIcon());
//            }
//        });
    }
}
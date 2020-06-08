package plugindata;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import plugindata.entities.SoftwareComponent;
import plugindata.utils.component_identification.ComponentDataHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AttachLoggerAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        ComponentDataHandler.retrievePackageDataInfo(e.getProject());
        addAJWeaverJarFile(getEventProject(e));
        File f = new File(getClass().getClassLoader().getResource("LoggingAspect.java").getFile());
        VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(f);
        addLoggingAspectFile(getEventProject(e), vf);
        addAOPConfig(getEventProject(e));
        f = new File(getEventProject(e).getBasePath() + "/src/executiondatalogging/LoggingAspect.java");
        vf = LocalFileSystem.getInstance().findFileByIoFile(f);
        Module m = ModuleUtil.findModuleForFile(vf, getEventProject(e));
        addAJLib(m);
    }

    private void addAJWeaverJarFile(Project project) {

        File ajw = new File(getClass().getClassLoader().getResource("aspectjweaver.jar").getFile());
        VirtualFile f = LocalFileSystem.getInstance().findFileByIoFile(ajw);
        VirtualFile dir = LocalFileSystem.getInstance().findFileByIoFile(new File(project.getBasePath()));

        if (!new File(dir.getPath() + "\\aspectjweaver.jar").exists())
            RunnableHelper.runWriteCommand(project, new Runnable() {
                @Override
                public void run() {
                    RunnableHelper.handleFileCopy(this, f, dir);
                }
            });
    }

    private void addLoggingAspectFile(Project project, VirtualFile f) {
        RunnableHelper.runWriteCommand(project, new Runnable() {
            @Override
            public void run() {
                RunnableHelper.handleDirCreation(this, project.getBasePath() + "/src/executiondatalogging");
            }
        });

        File d = new File(project.getBasePath() + "/src/executiondatalogging");
        VirtualFile dir = LocalFileSystem.getInstance().findFileByIoFile(d);

        if (!new File(dir.getPath() + "\\LoggingAspect.java").exists())
            RunnableHelper.runWriteCommand(project, new Runnable() {
                @Override
                public void run() {
                    RunnableHelper.handleFileCopy(this, f, dir);
                }
            });
    }

    private void addAOPConfig(Project project) {

        File config = new File(getClass().getClassLoader().getResource("aop.xml").getFile());
        try {
            FileWriter fw = new FileWriter(config.getAbsolutePath());
            fw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<aspectj>\n" +
                    "    <aspects>\n" +
                    "        <concrete-aspect name=\"executiondatalogging.ConcreteAspect\"\n" +
                    "                         extends=\"executiondatalogging.LoggingAspect\">\n" +
                    "            <pointcut name=\"methodExecuted\"\n" +
                    "                      expression=" + buildPointcutExpression(project) +
                    " />\n" +
                    "        </concrete-aspect>\n" +
                    "    </aspects>\n" +
                    "</aspectj>");
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        VirtualFile f = LocalFileSystem.getInstance().findFileByIoFile(config);

        RunnableHelper.runWriteCommand(project, new Runnable() {
            @Override
            public void run() {
                RunnableHelper.handleDirCreation(this, project.getBasePath() + "/src/META-INF");
            }
        });

        File d = new File(project.getBasePath() + "/src/META-INF");
        VirtualFile dir = LocalFileSystem.getInstance().findFileByIoFile(d);

        if (!new File(dir.getPath() + "\\aop.xml").exists())
            RunnableHelper.runWriteCommand(project, new Runnable() {
                @Override
                public void run() {
                    RunnableHelper.handleFileCopy(this, f, dir);
                }
            });
    }

    private String buildPointcutExpression(Project project) {
        List<String> packages = getMainPackages(project);
        if (packages.isEmpty())
            return "\"\"";

        String res = "\"call(* " + packages.get(0) + "..*(..)) || call(" + packages.get(0) +
                ".*.new(..))";
        for (int i = 1; i < packages.size(); i++)
            res += " || call(* " + packages.get(i) + "..*(..)) || call(" + packages.get(i) +
                    ".*.new(..))";
        res += "\"";
        return res;
    }

    private List<String> getMainPackages(Project project) {

        List<String> packages = new ArrayList<>();
        File f = new File(project.getBasePath() + "/src");
        if (!f.exists())
            return packages;
        for (File child : f.listFiles())
            if (child.isDirectory() && !child.getName().equals("executiondatalogging"))
                packages.add(child.getName());
        return packages;
    }

    private void addAJLib(Module module) {

        ApplicationManager.getApplication().runWriteAction(() -> {

            String jarFilePath = getClass().getClassLoader().getResource("aspectjrt.jar").getPath();

            final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
            final String clzUrlString = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, jarFilePath) + JarFileSystem.JAR_SEPARATOR;
            final VirtualFile jarVirtualFile = VirtualFileManager.getInstance().findFileByUrl(clzUrlString);
            final ModifiableRootModel modifiableModel = rootManager.getModifiableModel();

            Library library = modifiableModel.getModuleLibraryTable().getLibraryByName("aspectjrt");
            if (library == null) {
                library = modifiableModel.getModuleLibraryTable().createLibrary("aspectjrt");
                Library.ModifiableModel libraryModel = library.getModifiableModel();

                if (jarVirtualFile != null)
                    libraryModel.addRoot(jarVirtualFile, OrderRootType.CLASSES);

                libraryModel.commit();
            }
            modifiableModel.commit();
        });
    }
}
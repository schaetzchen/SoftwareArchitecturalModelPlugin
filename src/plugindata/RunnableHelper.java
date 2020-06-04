package plugindata;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RunnableHelper {

    public static void runWriteCommand(Project project, Runnable command) {
        CommandProcessor.getInstance().executeCommand(project, new WriteAction(command), "Foo", "Bar");
    }

    public static void runReadCommand(Project project, Runnable command) {
        CommandProcessor.getInstance().executeCommand(project, new ReadAction(command), "Foo", "Bar");
    }

//    public static void createDirectoryCommand(Project project, Runnable command) {
//        CommandProcessor.getInstance().executeCommand(project, new WriteAction(command), "Foo2", "Bar2");
//    }

    static class WriteAction implements Runnable {
        Runnable cmd;

        WriteAction(Runnable cmd) {
            this.cmd = cmd;
        }
        public void run() {
            ApplicationManager.getApplication().runWriteAction(cmd);
        }
    }

    static class ReadAction implements Runnable {
        Runnable cmd;

        ReadAction(Runnable cmd) { this.cmd = cmd; }
        public void run() { ApplicationManager.getApplication().runReadAction(cmd); }
    }

//    static class CreateDir implements Runnable {
//        Runnable cmd;
//
//        CreateDir(Runnable cmd) {this.cmd = cmd;}
//        public void run() { ApplicationManager.getApplication().runWriteAction(cmd);}
//    }

    private RunnableHelper() {}

    public static VirtualFile handleFileCopy(Object requestor, @NotNull VirtualFile file, @NotNull VirtualFile toDir) {

        try {
            return VfsUtil.copy(requestor, file, toDir);
        }
        catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }
        return null;
    }

    public static void handleDirCreation(Object requestor, String path) {

        try {
            VfsUtil.createDirectoryIfMissing(path);
        }
        catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }
    }

    public static Set<String> getPackageData(Project project) {

        Collection<VirtualFile> virtualFiles =
                FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, JavaFileType.INSTANCE,
                        GlobalSearchScope.projectScope(project));

        Set<String> res = new HashSet<>();

        for (VirtualFile vf: virtualFiles) {
            PsiFile psifile = PsiManager.getInstance(project).findFile(vf);

            if (psifile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psifile;
                String PackageName = psiJavaFile.getPackageName();
                PsiPackage pack = JavaPsiFacade.getInstance(project).findPackage(PackageName);
                res.add(pack.getQualifiedName());
            }
        }

        return res;
    }
}

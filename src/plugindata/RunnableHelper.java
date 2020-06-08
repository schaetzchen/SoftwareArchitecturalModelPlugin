package plugindata;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.apache.commons.io.FileDeleteStrategy;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

        ReadAction(Runnable cmd) {
            this.cmd = cmd;
        }

        public void run() {
            ApplicationManager.getApplication().runReadAction(cmd);
        }
    }

    private RunnableHelper() {
    }

    public static VirtualFile handleFileCopy(Object requestor, @NotNull VirtualFile file, @NotNull VirtualFile toDir) {

        try {
            return VfsUtil.copy(requestor, file, toDir);
        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }
        return null;
    }

    public static void handleDirCreation(Object requestor, String path) {

        try {
            VfsUtil.createDirectoryIfMissing(path);
        } catch (IOException ex) {
            System.out.println(ex.getStackTrace());
        }
    }

    public static void deleteFile(Object requestor, String path) {
        File f = new File(path);
        try {
            if (f.exists())
                LocalFileSystem.getInstance().findFileByIoFile(f).delete(requestor);
        } catch (IOException | NullPointerException ex) {
            try {
                if (f.exists())
                    FileDeleteStrategy.FORCE.delete(f);
            } catch (IOException e) {
            }
        }
    }

    public static Set<String> getPackageData(Project project) {

        try {
            File f = new File(project.getBasePath() + "/src");
            VirtualFile srcDir = LocalFileSystem.getInstance().findFileByIoFile(f);
            Set<String> res = new HashSet<>();
            recur(srcDir, res, project);
            return res;
        }
//        try {
//            Collection<VirtualFile> virtualFiles =
//                    FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, JavaFileType.INSTANCE,
//                            GlobalSearchScope.projectScope(project));
//
//            Set<String> res = new HashSet<>();
//
//            for (VirtualFile vf : virtualFiles) {
//                PsiFile psifile = PsiManager.getInstance(project).findFile(vf);
//
//                if (psifile instanceof PsiJavaFile) {
//                    PsiJavaFile psiJavaFile = (PsiJavaFile) psifile;
//                    String PackageName = psiJavaFile.getPackageName();
//                    if (!PackageName.equals("executiondatalogging"))
//                        res.add(JavaPsiFacade.getInstance(project).findPackage(PackageName).getQualifiedName());
//                }
//            }
//        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return new HashSet<>();
    }

    private static void recur(VirtualFile vf, Set<String> res, Project project) {

        if (vf.isDirectory()) {
            for (VirtualFile child : vf.getChildren())
                if (child.isDirectory())
                    recur(child, res, project);
        }
        for (File temp : new File(vf.getPath()).listFiles()) {
            VirtualFile f = LocalFileSystem.getInstance().findFileByIoFile(temp);
            PsiFile psiFile = PsiManager.getInstance(project).findFile(f);
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                String PackageName = psiJavaFile.getPackageName();
                if (!PackageName.equals("executiondatalogging"))
                    res.add(JavaPsiFacade.getInstance(project).findPackage(PackageName).getQualifiedName());
            }
        }
    }
}

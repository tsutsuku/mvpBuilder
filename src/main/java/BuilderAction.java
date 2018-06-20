import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiShortNamesCache;
import org.apache.commons.lang.WordUtils;

public class BuilderAction extends AnAction {
    private static final String CONTRACT = "Contract";
    private static final String MODEL = "Model";
    private static final String PRESENTER = "Presenter";


    private String mName;

//    private Editor mEditor;
    private Project mProject;
    private PsiFileFactory mPsiFileFactory;
    private PsiShortNamesCache mNamesCache;
    private PsiDirectory curDirectory;

    @Override
    public void actionPerformed(AnActionEvent e) {
        mProject = e.getData(PlatformDataKeys.PROJECT);
//        mEditor = e.getData(PlatformDataKeys.EDITOR);

//        if (mEditor != null && mProject != null) {
        if (mProject != null) {
            mPsiFileFactory = PsiFileFactory.getInstance(mProject);
            mNamesCache = PsiShortNamesCache.getInstance(mProject);

            curDirectory = (PsiDirectory) e.getData(CommonDataKeys.PSI_ELEMENT);
            mName = WordUtils.capitalize(curDirectory.getParent().getName() + " " + curDirectory.getName()).replace(" ", "");

            createContract(curDirectory);

        }
    }

    /**
     * 创建mvp目录
     *
     * @param mvp
     * @param originalDirectory
     * @return
     */
    private PsiDirectory createDirectory(String mvp, PsiDirectory originalDirectory) {
        PsiDirectory mvpDirectory = originalDirectory.findSubdirectory(mvp);
        if (mvpDirectory == null) {
            mvpDirectory = originalDirectory.createSubdirectory(mvp);
        }
        return mvpDirectory;
    }

    private PsiClass createPresenter(PsiDirectory directory) {
        PsiFile presenterFile = directory.findFile(getInterJavaFile(PRESENTER));
        PsiClass presenterInter = null;
        if (presenterFile == null) {
            presenterInter = (PsiClass) directory.add(createPresenterInter(getInterJavaFile(PRESENTER)));
        } else {
            presenterInter = mNamesCache.getClassesByName(getInterJavaFile(PRESENTER), directory.getResolveScope())[0];
        }
        return presenterInter;
    }

    private PsiClass createPresenterInter(String mName) {
        JavaFileType type = JavaFileType.INSTANCE;
        StringBuilder builder = new StringBuilder();


        builder.append("@NonNull\n");
        builder.append("private " + mName + "Contract.View view;\n");
        builder.append("@NonNull\n");
        builder.append("private " + mName + "Contract.Model model;\n");
        builder.append("@NonNull\n");
        builder.append("private CompositeDisposable mSubscription;\n");
        builder.append("@NonNull\n");
        builder.append("private BaseSchedulerProvider schedulerProvider;\n\n");

        builder.append("public " + mName + "Presenter(@NonNull " + mName + "Contract.Model model,\n"
                + "@NonNull " + mName + "Contract.View view,\n"
                + "@NonNull BaseSchedulerProvider schedulerProvider) {\n");
        builder.append("this.model = checkNotNull(model, \"model cannot be null\");\n");
        builder.append("this.view = checkNotNull(view, \"view cannot be null\");\n");
        builder.append("this.schedulerProvider = checkNotNull(\"schedulerProvider cannot be null\");\n");
        builder.append("mSubscription = new CompositeDisposable();\n");
        builder.append("view.setPresenter(this);\n}");

        return null;
    }

    /**
     * 创建Contract 类
     *
     * @param contractDirectory
     * @return
     */
    private PsiClass createContract(PsiDirectory contractDirectory) {
        PsiFile contractFile = contractDirectory.findFile(getInterJavaFile(CONTRACT));
        PsiClass contractInter = null;
        if (contractFile == null) {
            PsiClass psiClass = createContractInterJava(getImplJavaClass(CONTRACT));
            contractInter = (PsiClass) contractDirectory.add(psiClass);
        } else {
            contractInter = mNamesCache.getClassesByName(getImplJavaClass(CONTRACT), contractDirectory.getResolveScope())[0];
        }
        return contractInter;
    }

    /**
     * 创建Contract 模板类
     *
     * @param mName
     * @return
     */
    private PsiClass createContractInterJava(String mName) {
        JavaFileType type = JavaFileType.INSTANCE;
        StringBuilder builder = new StringBuilder();
        builder.append("public class " + mName + "{");
//        builder.append("public interface View extends BaseView<Presenter> {\n\n}\n");
//        builder.append("public interface Presenter extends BasePresenter {\n\n}\n");
        builder.append("public interface Model {}");
        builder.append("}");
        return ((PsiJavaFile) mPsiFileFactory.createFileFromText(mName, type, builder.toString())).getClasses()[0];
    }


    /**
     * 获取带后缀名Java的class文件
     *
     * @param mvp
     * @return
     */
    private String getInterJavaFile(String mvp) {
        StringBuilder builder = new StringBuilder();
        builder.append(mName);
        builder.append(mvp);
        builder.append(".java");
        return builder.toString();
    }

    /**
     * 获取不带后缀名Java 的class类
     *
     * @param mvp
     * @return
     */
    private String getImplJavaClass(String mvp) {
        StringBuilder builder = new StringBuilder();
        builder.append(mName);
        builder.append(mvp);
        return builder.toString();
    }
}

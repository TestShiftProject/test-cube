package org.testshift.testcube.model;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestCaseBranchCoverageJSON;
import eu.stamp_project.dspot.common.report.output.selector.branchcoverage.json.TestClassBranchCoverageJSON;
import eu.stamp_project.dspot.common.report.output.selector.extendedcoverage.json.TestCaseJSON;
import eu.stamp_project.dspot.selector.branchcoverageselector.BranchCoverage;
import eu.stamp_project.dspot.selector.branchcoverageselector.LineCoverage;
import eu.stamp_project.dspot.selector.extendedcoverageselector.CoverageImprovement;
import eu.stamp_project.dspot.selector.extendedcoverageselector.ExtendedCoverage;
import org.testshift.testcube.misc.Util;

import java.util.*;

public class GenerationResult {
    private static final Logger logger = Logger.getInstance(GenerationResult.class);
    public Project project;
    public String testClass;
    public Set<String> initialCoveredLines;
    private Set<Util.Branch> initialCoveredBranches;;
    public List<GeneratedTestCase> generatedTestCases = new ArrayList<>();
    private PsiClass originalClass;

    private GenerationResult(Project project, String testClass, Set<String> initialCoveredLines,
                             Set<Util.Branch> initialCoveredBranches) {
        this.project = project;
        this.testClass = testClass;
        this.initialCoveredLines = initialCoveredLines;
        this.initialCoveredBranches = initialCoveredBranches;
    }
    public static GenerationResult buildGenerationResult(Project project, String testClass,
                                                         Set<String> initialCoveredLines, Set<Util.Branch> initialCoveredBranches){
        GenerationResult result = new GenerationResult(project, testClass, initialCoveredLines,
                                                           initialCoveredBranches);
        TestClassBranchCoverageJSON coverageResult = (TestClassBranchCoverageJSON) Util.getBranchCoverageJSON(project,
                                                                                                              testClass);
        if (coverageResult == null) {
            logger.warn("Json result file not found!");
            return result;
        }

        String originalTestClassPath = Util.getOriginalTestClassPath(project, testClass);
        VirtualFile originalFile = LocalFileSystem.getInstance().findFileByPath(originalTestClassPath);
        if (originalFile != null) {
            PsiJavaFile psiFile = (PsiJavaFile) JavaPsiFacade.getInstance(project)
                                                 .findClass(testClass,
                                                            GlobalSearchScope.everythingScope(result.project))
                                                 .getContainingFile();
            result.originalClass = Arrays.stream(psiFile.getClasses())
                  .filter((PsiClass c) -> c.getQualifiedName().equals(testClass))
                  .findFirst()
                  .get();
        }


        List<TestCaseBranchCoverageJSON> testCaseBranchCoverageJSONList = coverageResult.getTestCases();

        String amplifiedTestClassPath = Util.getAmplifiedTestClassPath(project, testClass);
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(amplifiedTestClassPath);

        if (file != null) {
            PsiJavaFile psiFile = (PsiJavaFile) PsiManager.getInstance(project).findFile(file);
            if (psiFile != null) {
                PsiClass psiClass = Arrays.stream(psiFile.getClasses())
                                          .filter((PsiClass c) -> c.getQualifiedName().equals(testClass))
                                          .findFirst()
                                          .get();
                PsiMethod[] methods = psiClass.getMethods();

                if (methods.length != testCaseBranchCoverageJSONList.size()) {
                    logger.warn("Count of methods found in amplified class: " + methods.length + " does not match " +
                                "with match with count of amplified methods reported: " +
                                testCaseBranchCoverageJSONList.size());
                }

                for (PsiMethod method : methods) {
                    Optional<TestCaseBranchCoverageJSON> testCaseJSON = testCaseBranchCoverageJSONList
                                                                    .stream()
                                                                    .filter(tcj -> tcj.getName()
                                                                                      .equals(method.getName()))
                                                                    .findAny();

                    Set<String> newCoveredLines = computeNewCoveredLines(testCaseJSON.get(), initialCoveredLines);
                    Set<Util.Branch> newCoveredBranches = computeNewCoveredBranches(testCaseJSON.get(),
                                                                                    initialCoveredBranches);

                    if (testCaseJSON.isPresent()) {
                        result.generatedTestCases.add(
                                new GeneratedTestCase(amplifiedTestClassPath, method.getName(), method,  psiFile,
                                                      testCaseJSON.get().getNbAssertionAdded(),
                                                      testCaseJSON.get().getNbInputAdded(), newCoveredLines,
                                                      newCoveredBranches));
                    } else {
                        logger.warn("Found no matching json result for test case " + method.getName());
                    }
                }
            }
        }
        return result;
    }

    private static Set<String> computeNewCoveredLines(TestCaseBranchCoverageJSON testCaseJSON, Set<String> initialCoveredLines){
        Set<String> newCoveredLines = new HashSet<>();
        List<LineCoverage> lineCoverages =  testCaseJSON.getLineCoverageList();
        for(LineCoverage lineCoverage: lineCoverages){
            if(!initialCoveredLines.contains(lineCoverage.getLine())){
                newCoveredLines.add(lineCoverage.getLine()+"");
            }
        }
        return newCoveredLines;
    }

    private static Set<Util.Branch> computeNewCoveredBranches(TestCaseBranchCoverageJSON testCaseJSON,
                                                              Set<Util.Branch> initialCoveredBranches){
        Set<Util.Branch> newCoveredBranches = new HashSet<>();
        List<BranchCoverage> branchCoverages = testCaseJSON.getBranchCoverageList();
        for(BranchCoverage branchCoverage: branchCoverages){
            Util.Branch branchTrue = new Util.Branch(branchCoverage.getRegion().getStartLine()+"", "True");
            if(branchCoverage.getTrueHitCount()>0 && !initialCoveredBranches.contains(branchTrue)){
                newCoveredBranches.add(branchTrue);
            }
            Util.Branch branchFalse = new Util.Branch(branchCoverage.getRegion().getStartLine()+"", "False");
            if(branchCoverage.getFalseHitCount()>0 && !initialCoveredBranches.contains(branchFalse)){
                newCoveredBranches.add(branchFalse);
            }
        }
        return newCoveredBranches;
    }

    public PsiClass getOriginalClass() {
        return originalClass;
    }
}

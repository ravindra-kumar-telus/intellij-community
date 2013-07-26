package com.jetbrains.python.documentation;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: catherine
 */
public class DocStringUtil {
  private DocStringUtil() {
  }

  public static String trimDocString(String s) {
    return s.trim()
            .replaceFirst("^((:py)?:class:`[~!]?|[A-Z]\\{)", "")
            .replaceFirst("(`|\\})?\\.?$", "");
  }

  @Nullable
  public static String getDocStringValue(@NotNull PyDocStringOwner owner) {
    return PyPsiUtils.strValue(owner.getDocStringExpression());
  }

  @Nullable
  public static StructuredDocString parse(@Nullable String text) {
    if (text == null) {
      return null;
    }
    if (isSphinxDocString(text)) {
      return new SphinxDocString(text);
    }
    return new EpydocString(text);
  }

  public static boolean isSphinxDocString(@NotNull String text) {
    return text.contains(":param ") || text.contains(":rtype") || text.contains(":type");
  }

  public static boolean isEpydocDocString(@NotNull String text) {
    return text.contains("@param ") || text.contains("@rtype") || text.contains("@type");
  }

  /**
   * Looks for a doc string under given parent.
   * @param parent where to look. For classes and functions, this would be PyStatementList, for modules, PyFile.
   * @return the defining expression, or null.
   */
  @Nullable
  public static PyStringLiteralExpression findDocStringExpression(@Nullable PyElement parent) {
    if (parent != null) {
      PsiElement seeker = PyUtil.getFirstNonCommentAfter(parent.getFirstChild());
      if (seeker instanceof PyExpressionStatement) seeker = PyUtil.getFirstNonCommentAfter(seeker.getFirstChild());
      if (seeker instanceof PyStringLiteralExpression) return (PyStringLiteralExpression)seeker;
    }
    return null;
  }

  public static StructuredDocString getStructuredDocString(PyDocStringOwner owner) {
    return parse(owner.getDocStringValue());
  }

  public static boolean isDocStringExpression(@Nullable PyExpression expression) {
    final PyDocStringOwner docStringOwner = PsiTreeUtil.getParentOfType(expression, PyDocStringOwner.class);
    if (docStringOwner != null) {
      if (docStringOwner.getDocStringExpression() == expression) {
        return true;
      }
    }
    if (expression instanceof PyStringLiteralExpression) {
      return isVariableDocString((PyStringLiteralExpression)expression);
    }
    return false;
  }

  @Nullable
  public static PyStringLiteralExpression getAttributeDocString(@NotNull PyTargetExpression attr) {
    if (attr.getParent() instanceof PyAssignmentStatement) {
      final PyAssignmentStatement assignment = (PyAssignmentStatement)attr.getParent();
      PsiElement nextSibling = assignment.getNextSibling();
      while (nextSibling != null && (nextSibling instanceof PsiWhiteSpace || nextSibling instanceof PsiComment)) {
        nextSibling = nextSibling.getNextSibling();
      }
      if (nextSibling instanceof PyExpressionStatement) {
        final PyExpression expression = ((PyExpressionStatement)nextSibling).getExpression();
        if (expression instanceof PyStringLiteralExpression) {
          return (PyStringLiteralExpression)expression;
        }
      }
    }
    return null;
  }

  @Nullable
  public static String getAttributeDocComment(@NotNull PyTargetExpression attr) {
    if (attr.getParent() instanceof PyAssignmentStatement) {
      final PyAssignmentStatement assignment = (PyAssignmentStatement)attr.getParent();
      PsiElement prevSibling = assignment.getPrevSibling();
      while (prevSibling != null && (prevSibling instanceof PsiWhiteSpace)) {
        prevSibling = prevSibling.getPrevSibling();
      }
      if (prevSibling instanceof PsiComment && prevSibling.getText().startsWith("#:")) {
        return prevSibling.getText().substring(2);
      }
    }
    return null;
  }

  public static boolean isVariableDocString(@NotNull PyStringLiteralExpression expr) {
    final Module module = ModuleUtilCore.findModuleForPsiElement(expr);
    if (module == null) return false;
    final PyDocumentationSettings settings = PyDocumentationSettings.getInstance(module);
    if (settings.isEpydocFormat(expr.getContainingFile()) || settings.isReSTFormat(expr.getContainingFile())) {
      final PsiElement parent = expr.getParent();
      if (!(parent instanceof PyExpressionStatement)) {
        return false;
      }
      PsiElement prevElement = parent.getPrevSibling();
      while (prevElement instanceof PsiWhiteSpace || prevElement instanceof PsiComment) {
        prevElement = prevElement.getPrevSibling();
      }
      if (prevElement instanceof PyAssignmentStatement) {
        if (expr.getText().contains("type:")) return true;

        final PyAssignmentStatement assignmentStatement = (PyAssignmentStatement)prevElement;
        final ScopeOwner scope = PsiTreeUtil.getParentOfType(prevElement, ScopeOwner.class);
        if (scope instanceof PyClass || scope instanceof PyFile) {
          return true;
        }
        if (scope instanceof PyFunction) {
          for (PyExpression target : assignmentStatement.getTargets()) {
            if (PyUtil.isInstanceAttribute(target)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }
}

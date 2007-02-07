/*
 * Created by IntelliJ IDEA.
 * User: max
 * Date: Aug 20, 2006
 * Time: 8:40:15 PM
 */
package com.intellij.openapi.progress.impl;

import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.TaskInfo;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.util.ProgressWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.ex.StatusBarEx;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class BackgroundableProcessIndicator extends ProgressWindow {
  protected final StatusBarEx myStatusBar;

  @SuppressWarnings({"FieldAccessedSynchronizedAndUnsynchronized"})

  private PerformInBackgroundOption myOption;
  private TaskInfo myInfo;

  public BackgroundableProcessIndicator(Task.Backgroundable task) {
    this(task.getProject(), task, task);    
  }

  public BackgroundableProcessIndicator(Project project, TaskInfo info, PerformInBackgroundOption option) {
    super(info.isCancellable(), true, project, info.getCancelText());
    myOption = option;
    myInfo = info;
    setTitle(info.getTitle());
    myStatusBar = (StatusBarEx)WindowManager.getInstance().getStatusBar(project);
    if (option.shouldStartInBackground()) {
      doBackground();
    }
  }

  public BackgroundableProcessIndicator(Project project,
                                        @Nls final String progressTitle,
                                        @NotNull PerformInBackgroundOption option,
                                        @Nls final String cancelButtonText,
                                        @Nls final String backgroundStopTooltip, final boolean cancellable) {
    this(project, new TaskInfo() {
      public String getTitle() {
        return progressTitle;
      }

      public String getCancelText() {
        return cancelButtonText;
      }

      public String getCancelTooltipText() {
        return backgroundStopTooltip;
      }

      public boolean isCancellable() {
        return cancellable;
      }
    }, option);
  }


  protected void showDialog() {
    if (myOption.shouldStartInBackground()) {
      return;
    }

    super.showDialog();
  }

  public void background() {
    myOption.processSentToBackground();
    doBackground();
    super.background();
  }

  private void doBackground() {
    myStatusBar.add(this, myInfo);
  }
}
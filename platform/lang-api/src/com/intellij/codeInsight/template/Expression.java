/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInsight.template;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupFocusDegree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Expression {
  @Nullable
  public abstract Result calculateResult(ExpressionContext context);

  @Nullable
  public abstract Result calculateQuickResult(ExpressionContext context);

  public abstract LookupElement @Nullable [] calculateLookupItems(ExpressionContext context);

  @Nullable
  public String getAdvertisingText() {
    return null;
  }

  /**
   * @return true if {@link Expression#calculateResult(com.intellij.codeInsight.template.ExpressionContext)} or
   *                 {@link Expression#calculateQuickResult(com.intellij.codeInsight.template.ExpressionContext)}
   *         require committed PSI for their calculation or false otherwise
   */
  public boolean requiresCommittedPSI() {
    return true;
  }

  /**
   * @return focus degree to use for expression's lookup.
   * @see LookupFocusDegree
   */
  @NotNull
  public LookupFocusDegree getLookupFocusDegree() {
    return LookupFocusDegree.FOCUSED;
  }
}

/*
 * Copyright 2013 Minas Manthos
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
package browsewordatcaret;

import com.intellij.openapi.editor.actions.EditorActionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BWACUtils {
    private BWACUtils() {
    }

    /**
     * Determines whether a character should be treated as part of a "word".
     *
     * <p>The original logic relied on {@link Character#isJavaIdentifierPart(char)} which treats
     * '$' as a valid identifier character. That works for Java, but breaks PHP navigation:
     * in PHP, the declaration uses <code>$status</code> while property access uses
     * <code>->status</code>. For PHP we want '$' to be a separator (a sigil), not part of the word.
     */
    private static boolean isWordChar(char c, boolean phpMode) {
        if (phpMode && c == '$') {
            return false;
        }
        return Character.isJavaIdentifierPart(c);
    }

    /**
     * Liefert das Wort aus dem Text gem. index
     */
    @Nullable
    public static String extractWordFrom(@NotNull String text, int index) {
        return extractWordFrom(text, index, false);
    }

    /**
     * Extracts a word from text at the given index.
     *
     * @param phpMode when true, treats '$' as a separator (PHP sigil) so "$status" and "status"
     *                are considered the same word.
     */
    @Nullable
    public static String extractWordFrom(@NotNull String text, int index, boolean phpMode) {
        int length = text.length();
        if (length <= 0 || index < 0 || index > length) {
            return null;
        }

        // PHP quality-of-life: if caret sits on '$', treat the word as the identifier after it.
        if (phpMode && index < length && text.charAt(index) == '$') {
            index++;
            if (index > length) {
                return null;
            }
        }

        int begin = index;
        while (begin > 0 && isWordChar(text.charAt(begin - 1), phpMode)) {
            begin--;
        }
        int end = index;
        while (end < length && isWordChar(text.charAt(end), phpMode)) {
            end++;
        }
        if (end <= begin || (begin == 0 && end == length) ) {
            return null; // fix issue 5: komplettem text ausschliessen
        }
        return text.substring(begin, end);
    }

    /**
     * Prüft, ob bei begin/end ein Wort beginnt und endet.
     */
    public static boolean isStartEnd(@NotNull final String text, final int begin, final int end, boolean checkOnlyPreviousNext, boolean checkHumpBound) {
        return isStartEnd(text, begin, end, checkOnlyPreviousNext, checkHumpBound, false);
    }

    /**
     * Prüft, ob bei begin/end ein Wort beginnt und endet.
     *
     * @param phpMode when true, treats '$' as a separator (PHP sigil).
     */
    public static boolean isStartEnd(@NotNull final String text, final int begin, final int end, boolean checkOnlyPreviousNext, boolean checkHumpBound, boolean phpMode) {
        int length = text.length();
        if (length == 0 || begin < 0 || begin >= length || end <= 0 || end > length) {
            return false;
        }
        // previous char
        if (begin != 0 && isWordChar(text.charAt(begin - 1), phpMode)) {
            if (!checkHumpBound || !EditorActionUtil.isHumpBound(text, begin, true)) {
                return false;
            }
        }
        // next char
        if (end != length && isWordChar(text.charAt(end), phpMode)) {
            if (!checkHumpBound || !EditorActionUtil.isHumpBound(text, end, false)) {
                return false;
            }
        }
        // first/last char from text
        if (!checkOnlyPreviousNext) {
            if (!isWordChar(text.charAt(begin), phpMode) || !isWordChar(text.charAt(end - 1), phpMode)) {
                return false;
            }
        }
        return true;
    }
}

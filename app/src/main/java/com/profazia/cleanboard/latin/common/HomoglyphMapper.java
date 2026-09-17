/*
 * Copyright (C) 2025 Raimondas Rimkus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.profazia.cleanboard.latin.common;

import android.util.SparseIntArray;

/**
 * Maps Latin code points onto visually similar code points from other scripts.
 *
 * When the homoglyph mode is enabled, every character produced by the keyboard is passed through
 * {@link #map(int, String)}. The result looks like Latin text at a glance, but is made of entirely
 * different characters, so it will not match plain text searches for the Latin original.
 *
 * Only letters and digits are remapped. Spaces, punctuation and control characters are always
 * passed through unchanged, so word breaks and sentence structure keep working normally.
 */
public final class HomoglyphMapper {
    /** Identity mapping. No substitution is performed. */
    public static final String STYLE_OFF = "off";
    /** Greek and Coptic letters. */
    public static final String STYLE_GREEK = "greek";
    /** Cyrillic letters. */
    public static final String STYLE_CYRILLIC = "cyrillic";
    /** Cherokee syllabary. Uppercase only, so lowercase is mapped to uppercase shapes. */
    public static final String STYLE_CHEROKEE = "cherokee";
    /** Fullwidth Latin forms, as used for CJK typesetting. */
    public static final String STYLE_FULLWIDTH = "fullwidth";
    /** Mathematical sans-serif alphanumerics. */
    public static final String STYLE_MATH_SANS = "math_sans";
    /** Mathematical monospace alphanumerics. */
    public static final String STYLE_MATH_MONO = "math_mono";
    /** Mixed script, picking the closest shape available in any script. */
    public static final String STYLE_MIXED = "mixed";

    public static final String STYLE_DEFAULT = STYLE_GREEK;

    /**
     * Latin letters in the order the substitution tables below use: a-z, then A-Z.
     */
    private static final String LATIN_LETTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * Greek substitutions. Letters with no reasonable Greek lookalike (b, d, f, g, j, l, q, r, s,
     * w) fall back to shapes that are still recognisable in context.
     */
    private static final String GREEK_LETTERS =
            // a    b    c    d    e    f    g    h    i    j    k    l    m
            "αвϲԁεƒɡηιϳκḷℯ"
            // n    o    p    q    r    s    t    u    v    w    x    y    z
            + "ηορգгѕτυνᴡχγζ"
            // A    B    C    D    E    F    G    H    I    J    K    L    M
            + "ΑΒϹᎠΕϜɢΗΙЈΚḶΜ"
            // N    O    P    Q    R    S    T    U    V    W    X    Y    Z
            + "ΝΟΡϘᎡЅΤᴜᏙᎳΧΥΖ";

    /** Cyrillic substitutions. */
    private static final String CYRILLIC_LETTERS =
            // a    b    c    d    e    f    g    h    i    j    k    l    m
            "авсԁеғԁһіјкӀм"
            // n    o    p    q    r    s    t    u    v    w    x    y    z
            + "порԛгѕтцᴠѡхуʐ"
            // A    B    C    D    E    F    G    H    I    J    K    L    M
            + "АВСԀЕҒГНІЈКӀМ"
            // N    O    P    Q    R    S    T    U    V    W    X    Y    Z
            + "ИОРԚѓЅТЦѴѠХУΖ";

    /** Fullwidth forms. Contiguous ranges, so these are computed rather than tabulated. */
    private static final int FULLWIDTH_LOWER_A = 0xFF41;
    private static final int FULLWIDTH_UPPER_A = 0xFF21;
    private static final int FULLWIDTH_ZERO = 0xFF10;

    /** Mathematical sans-serif. Non-BMP contiguous ranges. */
    private static final int MATH_SANS_LOWER_A = 0x1D5BA;
    private static final int MATH_SANS_UPPER_A = 0x1D5A0;
    private static final int MATH_SANS_ZERO = 0x1D7E2;

    /** Mathematical monospace. Non-BMP contiguous ranges. */
    private static final int MATH_MONO_LOWER_A = 0x1D68A;
    private static final int MATH_MONO_UPPER_A = 0x1D670;
    private static final int MATH_MONO_ZERO = 0x1D7F6;

    /**
     * Mixed script substitutions, choosing the closest available lookalike for each letter
     * regardless of which script it comes from.
     */
    private static final String MIXED_LETTERS =
            // a    b    c    d    e    f    g    h    i    j    k    l    m
            "аƄϲԁеſɡһіϳкǀм"
            // n    o    p    q    r    s    t    u    v    w    x    y    z
            + "ոοрզгѕтսνաхуʐ"
            // A    B    C    D    E    F    G    H    I    J    K    L    M
            + "ΑΒϹᎠΕϜԌНІЈΚᏞΜ"
            // N    O    P    Q    R    S    T    U    V    W    X    Y    Z
            + "ΝОРϘᎡЅТՈᏙᎳХҮΖ";

    /** Cherokee substitutions. Cherokee lowercase is a recent addition with poor font coverage. */
    private static final String CHEROKEE_LETTERS =
            // a    b    c    d    e    f    g    h    i    j    k    l    m
            "ᎪᏴᏟᎠᎬᎡᎶᎻᎥᎫᏦᏞᎷ"
            // n    o    p    q    r    s    t    u    v    w    x    y    z
            + "ᏁᎣᏢᎩᏒᏚᎢᏏᏔᎳᎸᎤᏃ"
            // A    B    C    D    E    F    G    H    I    J    K    L    M
            + "ᎪᏴᏟᎠᎬᎡᎶᎻᎥᎫᏦᏞᎷ"
            // N    O    P    Q    R    S    T    U    V    W    X    Y    Z
            + "ᏁᎣᏢᎩᏒᏚᎢᏏᏔᎳᎸᎤᏃ";

    private static final SparseIntArray GREEK_MAP = buildMap(GREEK_LETTERS);
    private static final SparseIntArray CYRILLIC_MAP = buildMap(CYRILLIC_LETTERS);
    private static final SparseIntArray CHEROKEE_MAP = buildMap(CHEROKEE_LETTERS);
    private static final SparseIntArray MIXED_MAP = buildMap(MIXED_LETTERS);

    private HomoglyphMapper() {
        // This class is not publicly instantiable.
    }

    /**
     * Builds a Latin code point to substitute code point lookup from a substitution table.
     *
     * @param substitutes the substitutes for {@link #LATIN_LETTERS}, in the same order.
     * @return the populated lookup.
     */
    private static SparseIntArray buildMap(final String substitutes) {
        final SparseIntArray map = new SparseIntArray(LATIN_LETTERS.length());
        // The substitution tables are indexed by Latin letter, and every entry is a single BMP
        // code point, so a plain char-by-char walk lines the two up.
        final int length = Math.min(LATIN_LETTERS.length(), substitutes.length());
        for (int i = 0; i < length; ++i) {
            map.put(LATIN_LETTERS.charAt(i), substitutes.charAt(i));
        }
        return map;
    }

    /**
     * Returns whether the given style performs any substitution at all.
     *
     * @param style one of the {@code STYLE_*} constants.
     * @return whether characters are rewritten under this style.
     */
    public static boolean isEnabled(final String style) {
        return style != null && !STYLE_OFF.equals(style);
    }

    /**
     * Maps a single code point to its lookalike under the given style.
     *
     * @param codePoint the Latin code point to map.
     * @param style one of the {@code STYLE_*} constants.
     * @return the substitute code point, or {@code codePoint} if there is nothing to substitute.
     */
    public static int map(final int codePoint, final String style) {
        if (!isEnabled(style)) {
            return codePoint;
        }

        final boolean isLower = codePoint >= 'a' && codePoint <= 'z';
        final boolean isUpper = codePoint >= 'A' && codePoint <= 'Z';
        final boolean isDigit = codePoint >= '0' && codePoint <= '9';
        if (!isLower && !isUpper && !isDigit) {
            // Punctuation, spaces and everything outside ASCII alphanumerics are left alone so
            // that word boundaries and sentence structure survive the substitution.
            return codePoint;
        }

        switch (style) {
        case STYLE_FULLWIDTH:
            if (isDigit) {
                return FULLWIDTH_ZERO + (codePoint - '0');
            }
            return isLower
                    ? FULLWIDTH_LOWER_A + (codePoint - 'a')
                    : FULLWIDTH_UPPER_A + (codePoint - 'A');
        case STYLE_MATH_SANS:
            if (isDigit) {
                return MATH_SANS_ZERO + (codePoint - '0');
            }
            return isLower
                    ? MATH_SANS_LOWER_A + (codePoint - 'a')
                    : MATH_SANS_UPPER_A + (codePoint - 'A');
        case STYLE_MATH_MONO:
            if (isDigit) {
                return MATH_MONO_ZERO + (codePoint - '0');
            }
            return isLower
                    ? MATH_MONO_LOWER_A + (codePoint - 'a')
                    : MATH_MONO_UPPER_A + (codePoint - 'A');
        case STYLE_GREEK:
            return GREEK_MAP.get(codePoint, codePoint);
        case STYLE_CYRILLIC:
            return CYRILLIC_MAP.get(codePoint, codePoint);
        case STYLE_CHEROKEE:
            return CHEROKEE_MAP.get(codePoint, codePoint);
        case STYLE_MIXED:
            return MIXED_MAP.get(codePoint, codePoint);
        default:
            return codePoint;
        }
    }

    /**
     * Maps every character of a string to its lookalike under the given style.
     *
     * @param text the text to map.
     * @param style one of the {@code STYLE_*} constants.
     * @return the mapped text, or {@code text} itself if there is nothing to substitute.
     */
    public static String map(final String text, final String style) {
        if (!isEnabled(style) || text == null || text.isEmpty()) {
            return text;
        }

        final StringBuilder builder = new StringBuilder(text.length());
        // Walk by code point so that any surrogate pairs already present in the input survive.
        int offset = 0;
        while (offset < text.length()) {
            final int codePoint = text.codePointAt(offset);
            builder.appendCodePoint(map(codePoint, style));
            offset += Character.charCount(codePoint);
        }
        return builder.toString();
    }
}

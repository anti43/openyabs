/*
 This file is part of YaBS.

 YaBS is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 YaBS is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with YaBS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openyabs.handwerk;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Safe, dependency-free recursive-descent arithmetic evaluator for site
 * measurement formulas (Aufmaß / REB-style expressions such as
 * {@code "3,50 * 2,40 - 1,10 * 2,00"} or {@code "(4.2 + 3.8) * 2.5"}).
 * <p>
 * Supports {@code +}, {@code -}, {@code *}, {@code /}, unary signs, nested
 * parentheses, and both German comma ({@code ,}) and dot ({@code .}) decimal
 * separators. Never invokes {@code ScriptEngine} or external evaluation.
 */
public final class MeasurementEvaluator {

    private static final MathContext MC = new MathContext(12, RoundingMode.HALF_UP);

    private final String input;
    private int pos;

    private MeasurementEvaluator(String input) {
        this.input = input;
        this.pos = 0;
    }

    /**
     * Evaluates a measurement formula and rounds to 3 decimal places (standard
     * REB / VOB site measurement precision).
     *
     * @param formula arithmetic expression
     * @return evaluated result rounded to 3 decimals
     * @throws IllegalArgumentException if syntax is invalid or division by zero occurs
     */
    public static BigDecimal evaluate(String formula) {
        if (formula == null || formula.trim().isEmpty()) {
            throw new IllegalArgumentException("Measurement formula must not be empty");
        }
        // Normalize common multiplication symbols (x, ×, ·) and German decimal commas
        String normalized = formula.trim()
                .replace('\u00D7', '*')
                .replace('\u00B7', '*')
                .replace(',', '.');
        MeasurementEvaluator parser = new MeasurementEvaluator(normalized);
        BigDecimal value = parser.parseExpression();
        parser.skipWhitespace();
        if (parser.pos < parser.input.length()) {
            throw new IllegalArgumentException("Unexpected character '" + parser.input.charAt(parser.pos)
                    + "' at position " + (parser.pos + 1));
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal parseExpression() {
        BigDecimal left = parseTerm();
        while (true) {
            skipWhitespace();
            if (match('+')) {
                left = left.add(parseTerm(), MC);
            } else if (match('-')) {
                left = left.subtract(parseTerm(), MC);
            } else {
                return left;
            }
        }
    }

    private BigDecimal parseTerm() {
        BigDecimal left = parseFactor();
        while (true) {
            skipWhitespace();
            if (match('*')) {
                left = left.multiply(parseFactor(), MC);
            } else if (match('/')) {
                BigDecimal right = parseFactor();
                if (right.signum() == 0) {
                    throw new IllegalArgumentException("Division by zero in measurement formula");
                }
                left = left.divide(right, MC);
            } else {
                return left;
            }
        }
    }

    private BigDecimal parseFactor() {
        skipWhitespace();
        if (match('+')) {
            return parseFactor();
        }
        if (match('-')) {
            return parseFactor().negate();
        }
        if (match('(')) {
            BigDecimal inner = parseExpression();
            skipWhitespace();
            if (!match(')')) {
                throw new IllegalArgumentException("Missing closing parenthesis ')'");
            }
            return inner;
        }
        return parseNumber();
    }

    private BigDecimal parseNumber() {
        skipWhitespace();
        int start = pos;
        boolean hasDigit = false;
        boolean hasDot = false;
        while (pos < input.length()) {
            char ch = input.charAt(pos);
            if (ch >= '0' && ch <= '9') {
                hasDigit = true;
                pos++;
            } else if (ch == '.' && !hasDot) {
                hasDot = true;
                pos++;
            } else {
                break;
            }
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Expected number at position " + (start + 1));
        }
        try {
            return new BigDecimal(input.substring(start, pos));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + input.substring(start, pos), e);
        }
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private boolean match(char expected) {
        if (pos < input.length() && input.charAt(pos) == expected) {
            pos++;
            return true;
        }
        return false;
    }
}

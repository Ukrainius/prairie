/**
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
package com.intropro.prairie.unit.hive2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Created by presidentio on 9/28/15.
 */
public class StatementsSplitter {

    private static final Logger LOGGER = LogManager.getLogger(StatementsSplitter.class);

    public static final Pattern START_OF_COMMENT_PATTERN = Pattern.compile(".*\\s--", Pattern.DOTALL);
    public static final Pattern LAST_CHAR_NOT_ESCAPED_PATTERN = Pattern.compile(".*[^\\\\].", Pattern.DOTALL);

    public static final String SQL_SPECIAL_CHARS = ";\"'-\n";

    /**
     * Splits expression on ';'.
     * ';' within quotes (" or ') or comments ( -- ) are ignored.
     */
    public static List<String> splitStatements(String expression) {
        StringTokenizer tokenizer = new StringTokenizer(expression, SQL_SPECIAL_CHARS, true);

        List<String> statements = new ArrayList<String>();
        String statement = "";
        while (tokenizer.hasMoreElements()) {
            String token = (String) tokenizer.nextElement();
            switch (token) {

                // Close statement and start a new one
                case ";":
                    // Only add statement that is not empty
                    if (isValidStatement(statement)) {
                        statements.add(statement.trim());
                    }
                    statement = "";
                    break;

                // Preserve comments (--)
                case "-":
                    statement += token;
                    if (START_OF_COMMENT_PATTERN.matcher(statement).matches()) {
                        statement += readUntilEndOfLine(tokenizer);
                    }
                    break;

                // Preserve quotes
                case "\"":
                case "'":
                    statement += readQuoted(tokenizer, token);
                    break;

                // Add any other elements to the current statement
                default:
                    statement += token;
            }
        }

        // Only add statement that is not empty
        if (isValidStatement(statement)) {
            statements.add(statement);
        }
        return statements;
    }

    private static boolean isValidStatement(String statement) {
        // Empty strings does not validate.
        return statement.trim().length() > 0;
    }

    static String readUntilEndOfLine(StringTokenizer tokenizer) {
        String statement = "";

        while (tokenizer.hasMoreElements()) {
            statement += tokenizer.nextElement();
            if (statement.endsWith("\n")) {
                break;
            }
        }

        return statement;
    }

    static String readQuoted(StringTokenizer tokenizer, String startQuoteToken) {
        String quotedString = startQuoteToken;
        while (tokenizer.hasMoreElements()) {
            quotedString += (String) tokenizer.nextElement();
            // If the last char is an end of quote token and it was not escaped by the previous token, we break.
            if (quotedString.endsWith(startQuoteToken) &&
                    LAST_CHAR_NOT_ESCAPED_PATTERN.matcher(quotedString).matches()) {
                break;
            }
        }
        return quotedString;
    }

}

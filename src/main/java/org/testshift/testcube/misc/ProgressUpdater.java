package org.testshift.testcube.misc;

import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads test-cube's output to update progress indicator text.
 */
public class ProgressUpdater {
    private final ProgressIndicator indicator;
    private final Map<Pattern, PromptResolver> resolvers;

    private static final Pattern BUILDING_PATTERN = Pattern.compile(".*run maven.*(\n)?");
    private static final Pattern AMPLIFYING_PATTERN = Pattern.compile(
            ".*Amplification of (.*) \\(\\d.* test\\(s\\)\\).*");

    public ProgressUpdater(@NotNull ProgressIndicator indicator) {
        this.indicator = indicator;
        resolvers = new HashMap<>();
        resolvers.put(BUILDING_PATTERN, new LiteralResolver("Building project..."));
        resolvers.put(AMPLIFYING_PATTERN, new ExtractorResolver("Amplifying %s...", AMPLIFYING_PATTERN));
    }

    public void onNewLine(String line) {
        resolvers.entrySet()
                 .stream()
                 .filter(entry -> entry.getKey().matcher(line).matches())
                 .findFirst()
                 .ifPresent(entry -> indicator.setText(entry.getValue().resolve(line)));
    }

    private interface PromptResolver {
        String resolve(String line);
    }

    private static class LiteralResolver implements PromptResolver {
        private final String literal;

        LiteralResolver(String literal) {
            this.literal = literal;
        }

        @Override
        public String resolve(String line) {
            return literal;
        }
    }

    private static class ExtractorResolver implements PromptResolver {

        private final Pattern pattern;
        private final String format;

        public ExtractorResolver(String format, Pattern pattern) {
            this.format = format;
            this.pattern = pattern;
        }

        @Override
        public String resolve(String line) {
            Matcher matcher = pattern.matcher(line);
            boolean matches = matcher.matches();
            if (!matches) {
                throw new IllegalStateException("Invalid instance of " + ExtractorResolver.class);
            }
            return String.format(format, matcher.group(1));
        }
    }

}

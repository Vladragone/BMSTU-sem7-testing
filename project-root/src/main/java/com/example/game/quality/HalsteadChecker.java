package com.example.game.quality;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class HalsteadChecker {

    private static final Set<String> OPERATOR_KEYWORDS = Set.of(
            "if", "else", "for", "while", "do", "switch", "case", "default", "break", "continue",
            "return", "try", "catch", "finally", "throw", "throws", "new", "instanceof",
            "class", "interface", "enum", "extends", "implements", "package", "import",
            "public", "private", "protected", "static", "final", "abstract", "synchronized",
            "volatile", "transient", "native", "strictfp", "this", "super"
    );

    private static final Set<String> OPERATOR_SYMBOLS = Set.of(
            ">>>=", "<<=", ">>=", "++", "--", "&&", "||", "==", "!=", "<=", ">=", "->", "::",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<", ">>", ">>>",
            "=", "+", "-", "*", "/", "%", "&", "|", "^", "~", "!", "<", ">", "?", ":", ".", ","
    );

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            ">>>=" +
                    "|<<=|>>=|\\+\\+|--|&&|\\|\\||==|!=|<=|>=|->|::|\\+=|-=|\\*=|/=|%=|&=|\\|=|\\^=|<<|>>>|>>" +
                    "|[=+\\-*/%&|^~!<>?:.,]" +
                    "|\\b[0-9]+(?:\\.[0-9]+)?\\b" +
                    "|\\btrue\\b|\\bfalse\\b|\\bnull\\b" +
                    "|\\b[A-Za-z_][A-Za-z0-9_]*\\b"
    );

    private HalsteadChecker() {
    }

    public static void main(String[] args) throws IOException {
        String sourceRoot = args.length > 0 ? args[0] : "src/main/java";
        double maxVolume = args.length > 1 ? Double.parseDouble(args[1]) : 6000.0;
        List<FileMetric> metrics = new ArrayList<>();

        try (Stream<Path> files = Files.walk(Path.of(sourceRoot))) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> metrics.add(analyze(path)));
        }

        metrics.sort(Comparator.comparingDouble(FileMetric::volume).reversed());
        System.out.println("Halstead volume per file:");
        for (FileMetric metric : metrics) {
            System.out.printf("  %.2f  %s%n", metric.volume(), metric.path());
        }

        List<FileMetric> violations = metrics.stream()
                .filter(metric -> metric.volume() > maxVolume)
                .toList();

        if (!violations.isEmpty()) {
            System.err.printf("Halstead check failed: %d file(s) exceed max volume %.2f%n",
                    violations.size(), maxVolume);
            for (FileMetric violation : violations) {
                System.err.printf("  %.2f > %.2f : %s%n", violation.volume(), maxVolume, violation.path());
            }
            System.exit(1);
        }
    }

    private static FileMetric analyze(Path file) {
        try {
            String source = Files.readString(file, StandardCharsets.UTF_8);
            String sanitized = stripCommentsAndLiterals(source);
            Matcher matcher = TOKEN_PATTERN.matcher(sanitized);

            Set<String> uniqueOperators = new HashSet<>();
            Set<String> uniqueOperands = new HashSet<>();
            Map<String, Integer> operatorCounts = new HashMap<>();
            Map<String, Integer> operandCounts = new HashMap<>();

            while (matcher.find()) {
                String token = matcher.group();
                if (isOperator(token)) {
                    uniqueOperators.add(token);
                    operatorCounts.merge(token, 1, Integer::sum);
                } else {
                    uniqueOperands.add(token);
                    operandCounts.merge(token, 1, Integer::sum);
                }
            }

            int n1 = uniqueOperators.size();
            int n2 = uniqueOperands.size();
            int n = n1 + n2;
            int n1Total = operatorCounts.values().stream().mapToInt(Integer::intValue).sum();
            int n2Total = operandCounts.values().stream().mapToInt(Integer::intValue).sum();
            int nTotal = n1Total + n2Total;

            double volume = (n > 0 && nTotal > 0) ? (nTotal * (Math.log(n) / Math.log(2))) : 0.0;
            return new FileMetric(file.toString(), volume);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot analyze file: " + file, exception);
        }
    }

    private static boolean isOperator(String token) {
        return OPERATOR_SYMBOLS.contains(token) || OPERATOR_KEYWORDS.contains(token);
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static String stripCommentsAndLiterals(String source) {
        StringBuilder out = new StringBuilder(source.length());
        int i = 0;
        while (i < source.length()) {
            char ch = source.charAt(i);

            if (ch == '"' || ch == '\'') {
                char quote = ch;
                out.append(' ');
                i++;
                while (i < source.length()) {
                    char current = source.charAt(i);
                    if (current == '\\') {
                        i += 2;
                        continue;
                    }
                    i++;
                    if (current == quote) {
                        break;
                    }
                }
                continue;
            }

            if (ch == '/' && i + 1 < source.length()) {
                char next = source.charAt(i + 1);
                if (next == '/') {
                    i += 2;
                    while (i < source.length() && source.charAt(i) != '\n') {
                        i++;
                    }
                    continue;
                }
                if (next == '*') {
                    i += 2;
                    while (i + 1 < source.length()) {
                        if (source.charAt(i) == '*' && source.charAt(i + 1) == '/') {
                            i += 2;
                            break;
                        }
                        i++;
                    }
                    continue;
                }
            }

            out.append(ch);
            i++;
        }
        return out.toString();
    }

    private record FileMetric(String path, double volume) {
    }
}

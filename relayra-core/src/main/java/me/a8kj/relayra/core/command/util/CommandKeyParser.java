package me.a8kj.relayra.core.command.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandKeyParser {
    private static final Pattern ARG_PATTERN = Pattern.compile("\\{args\\[(\\d+)\\]\\}");

    public static String parse(String template, Object[] args) {
        if (template == null || !template.contains("{args[")) return template;

        Matcher matcher = ARG_PATTERN.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            if (index >= 0 && index < args.length) {
                matcher.appendReplacement(result, String.valueOf(args[index]));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
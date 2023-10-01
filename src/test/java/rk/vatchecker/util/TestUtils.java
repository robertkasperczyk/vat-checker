package rk.vatchecker.util;

import lombok.experimental.UtilityClass;
import org.assertj.core.util.Strings;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@UtilityClass
public class TestUtils {

    public static List<String> extractSqlStatements(String path) throws IOException {
        List<String> statements = new ArrayList<>();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(path)) {
            if (is == null) {
                return null;
            }
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return Arrays.stream(reader.lines().collect(joining(System.lineSeparator()))
                        .split(";"))
                        .toList();
            }
        }
    }

}

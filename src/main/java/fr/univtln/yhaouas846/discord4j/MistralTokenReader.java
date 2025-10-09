package fr.univtln.yhaouas846.discord4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MistralTokenReader {
    private static final String TOKEN_FILE = "mistral_token.txt";

    public static String readToken() throws IOException {
        return Files.readString(Paths.get(TOKEN_FILE)).trim();
    }
}

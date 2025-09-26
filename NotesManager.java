import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class NotesManager {
    private static final Path NOTES_DIR = Paths.get("notes");
    private static final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    public static void main(String[] args) {
        ensureNotesDir();
        while (true) {
            printMenu();
            String choice = prompt("Choose an option");
            switch (choice.trim()) {
                case "1" -> createMultipleNotes();
                case "2" -> viewNote();
                case "3" -> editNote();
                case "4" -> deleteNote();
                case "5" -> searchNotes();
                case "6" -> deleteAllNotes();
                case "0" -> {
                    System.out.println("Exiting...!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
            System.out.println();
        }
    }

    private static void ensureNotesDir() {
        try {
            if (Files.notExists(NOTES_DIR)) {
                Files.createDirectories(NOTES_DIR);
            }
        } catch (IOException e) {
            System.err.println("Failed to create notes directory: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void printMenu() {
        System.out.println("===== Notes Manager =====");
        System.out.println("1. Create new notes");
        System.out.println("2. View a note (by ID)");
        System.out.println("3. Edit a note (by ID)");
        System.out.println("4. Delete a note (by ID)");
        System.out.println("5. Search notes");
        System.out.println("6. Delete ALL notes");
        System.out.println("0. Exit");
    }

    private static String prompt(String message) {
        System.out.print(message + ": ");
        return scanner.nextLine();
    }

    // Generate next available ID
    private static int getNextId() {
        try {
            List<Path> files = Files.list(NOTES_DIR)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .toList();
            int maxId = 0;
            for (Path f : files) {
                String name = f.getFileName().toString();
                int underscoreIndex = name.indexOf("_");
                if (underscoreIndex > 0) {
                    try {
                        int id = Integer.parseInt(name.substring(0, underscoreIndex));
                        if (id > maxId) maxId = id;
                    } catch (NumberFormatException ignored) {}
                }
            }
            return maxId + 1;
        } catch (IOException e) {
            return 1;
        }
    }

    // Create multiple notes in one session
    private static void createMultipleNotes() {
        System.out.println("Enter multiple notes. Type 'done' as the title to stop.");
        while (true) {
            String title = prompt("Enter note title (or 'done' to finish)").trim();
            if (title.equalsIgnoreCase("done")) {
                System.out.println("Finished adding notes.");
                break;
            }
            if (title.isEmpty()) {
                System.out.println("Title cannot be empty.");
                continue;
            }

            int id = getNextId();
            String filename = id + "_" + sanitizeFilename(title) + ".txt"; // ✅ fixed
            Path file = NOTES_DIR.resolve(filename);

            System.out.println("Enter note content. End input with a single '.' on a new line.");
            List<String> lines = readMultiline();
            try {
                Files.write(file, lines, StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                System.out.println("Saved note: " + filename);
            } catch (IOException e) {
                System.err.println("Failed to save note: " + e.getMessage());
            }
        }
    }

    private static List<String> readMultiline() {
        List<String> lines = new ArrayList<>();
        while (true) {
            String line = scanner.nextLine();
            if (line.equals(".")) break;
            lines.add(line);
        }
        return lines;
    }

    private static void listNotes() {
        try {
            List<Path> files = Files.list(NOTES_DIR)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .sorted()
                    .collect(Collectors.toList());
            if (files.isEmpty()) {
                System.out.println("No notes found.");
                return;
            }
            System.out.println("Notes:");
            for (Path f : files) {
                System.out.printf("%s%n", filenameToTitle(f.getFileName().toString()));
            }
        } catch (IOException e) {
            System.err.println("Failed to list notes: " + e.getMessage());
        }
    }

    private static void viewNote() {
        Path file = chooseNoteById("view");
        if (file == null) return;
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            System.out.println("----- " + filenameToTitle(file.getFileName().toString()) + " -----");
            for (String line : lines) System.out.println(line);
            System.out.println("----- End -----");
        } catch (IOException e) {
            System.err.println("Failed to read note: " + e.getMessage());
        }
    }

    private static void editNote() {
        Path file = chooseNoteById("edit");
        if (file == null) return;

        System.out.println("Enter new content for the note. End input with a single '.' on a new line.");
        List<String> lines = readMultiline();
        try {
            Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("Note updated: " + file.getFileName());
        } catch (IOException e) {
            System.err.println("Failed to update note: " + e.getMessage());
        }
    }

    private static void deleteNote() {
        Path file = chooseNoteById("delete");
        if (file == null) return;

        String resp = prompt("Are you sure you want to delete '" +
                filenameToTitle(file.getFileName().toString()) + "'? (y/n)");
        if (!resp.equalsIgnoreCase("y")) {
            System.out.println("Cancelled.");
            return;
        }

        try {
            Files.delete(file);
            System.out.println("Deleted.");
        } catch (IOException e) {
            System.err.println("Failed to delete note: " + e.getMessage());
        }
    }

    private static void searchNotes() {
        String keyword = prompt("Enter search keyword").trim();
        if (keyword.isEmpty()) {
            System.out.println("Keyword cannot be empty.");
            return;
        }

        try {
            List<Path> files = Files.list(NOTES_DIR)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .collect(Collectors.toList());

            List<Path> matches = new ArrayList<>();
            for (Path f : files) {
                List<String> lines = Files.readAllLines(f, StandardCharsets.UTF_8);
                boolean found = lines.stream().anyMatch(l ->
                        l.toLowerCase().contains(keyword.toLowerCase()));
                if (found || filenameToTitle(f.getFileName().toString())
                        .toLowerCase().contains(keyword.toLowerCase())) {
                    matches.add(f);
                }
            }

            if (matches.isEmpty()) {
                System.out.println("No matches found for '" + keyword + "'.");
                return;
            }

            System.out.println("Matches:");
            for (Path f : matches) {
                System.out.println(filenameToTitle(f.getFileName().toString()));
            }
        } catch (IOException e) {
            System.err.println("Search failed: " + e.getMessage());
        }
    }

    // Choose note by entering ID
    private static Path chooseNoteById(String action) {
        listNotes();
        String s = prompt("Enter note ID to " + action);
        try {
            int id = Integer.parseInt(s.trim());
            List<Path> files = Files.list(NOTES_DIR)
                    .filter(p -> p.getFileName().toString().startsWith(id + "_"))
                    .toList();
            if (files.isEmpty()) {
                System.out.println("No note found with ID " + id);
                return null;
            }
            return files.get(0);
        } catch (IOException | NumberFormatException e) {
            System.out.println("Invalid ID.");
            return null;
        }
    }

    private static void deleteAllNotes() {
        String resp = prompt("Are you sure you want to delete ALL notes? (y/n)");
        if (!resp.equalsIgnoreCase("y")) {
            System.out.println("Cancelled.");
            return;
        }

        try {
            List<Path> files = Files.list(NOTES_DIR)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .toList();

            if (files.isEmpty()) {
                System.out.println("No notes to delete.");
                return;
            }

            for (Path f : files) {
                Files.deleteIfExists(f);
            }
            System.out.println("All notes deleted successfully!");
        } catch (IOException e) {
            System.err.println("Failed to delete all notes: " + e.getMessage());
        }
    }

    private static String sanitizeFilename(String title) {
        return title.replaceAll("[\\\\/:*?\\\"<>|]", "_")
                .replaceAll("\\s+", "_").trim();
    }

    private static String filenameToTitle(String filename) {
        if (filename.endsWith(".txt"))
            filename = filename.substring(0, filename.length() - 4);
        return filename.replaceAll("_", " ");
    }
}

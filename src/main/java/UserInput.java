import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.File;

public class UserInput {
    public static String getUserChoice() {
        Scanner scanner = new Scanner(System.in);

     
        List<String> fileNames = List.of(
                "acceleration",
                "blood_glucose",
                "blood_pressure",
                "body_fat_percentage",
                "body_mass_index",
                "body_temperature",
                "body_weight",
                "heart_rate",
                "kcal_burned",
                "orientation",
                "oxygen_saturation",
                "physical_activity",
                "speed",
                "step_count"
        );

        // Create a mapping dictionary
        Map<Integer, String> optionMapping = createOptionMapping(fileNames);

        // Display the options to the user
        displayOptions(optionMapping);

        // Get user choice
        int userChoice = getUserChoice(scanner, optionMapping.size());

        // Process the user's choice
        String selectedOption = optionMapping.get(userChoice);
        System.out.println("You chose: " + selectedOption);
//        src/main/resources/data/heart_rate

        return selectedOption;
    }

    public static String getUserFolder() {
    	 Scanner scanner = new Scanner(System.in);
         String folderPath = null;

         try {
             System.out.print("Enter the folder path: ");
             if (scanner.hasNextLine()) {
                 folderPath = scanner.nextLine();
             } else {
                 System.out.println("No input found.");
             }
         } catch (Exception e) {
             System.out.println("Error reading user input: " + e.getMessage());
         } finally {
             // Close the scanner in the finally block to ensure it gets closed
             if (scanner != null) {
                 scanner.close();
             }
         }

         return folderPath;
    }
    
    private static Map<Integer, String> createOptionMapping(List<String> fileNames) {
        Map<Integer, String> optionMapping = new HashMap<>();
        int optionNumber = 1;

        for (String fileName : fileNames) {
            optionMapping.put(optionNumber++, fileName);
        }

        return optionMapping;
    }

    private static void displayOptions(Map<Integer, String> optionMapping) {
        System.out.println("Choose an option:");

        // Print menu options in uppercase without underscores
        for (Map.Entry<Integer, String> entry : optionMapping.entrySet()) {
            String option = entry.getValue().toUpperCase().replace("_", " ");
            System.out.println(entry.getKey() + ". " + option);
        }
    }

    private static int getUserChoice(Scanner scanner, int maxOption) {
        int choice;

        do {
            System.out.print("Enter the number of your choice (1-" + maxOption + "): ");
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next(); // consume the invalid input
            }
            choice = scanner.nextInt();
        } while (choice < 1 || choice > maxOption);

        return choice;
    }
}

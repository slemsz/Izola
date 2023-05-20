package mycompany.app;

import java.io.IOException;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;
import java.net.http.HttpClient;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;
import com.google.gson.Gson;

public class AppUtils extends App {

    static String ENDPOINT_MEAL = "https://www.themealdb.com/api/json/v1/1";
    private String searchType;
    String searchItem;
    String json;
    private static final Gson gson = new Gson();
    String[] ingredientArray;
    String[] measurementArray;
    List<ResponseObject.MealDBMeal> meals = new ArrayList<>();
    List<CustomJsonObject.Meal> listOfMeals = new ArrayList<>();
    List<CustomJsonObject.Meal> processedMeals = new ArrayList<>();
    HashSet<String> knownIngredientsHashSet = new HashSet<>();
    HashSet<CustomJsonObject.Meal> knownMealHashSet = new HashSet<>();
    List<String> pantryItemList = new ArrayList<>();
    List<String> rawIngredients = new ArrayList<>();
    List<String> ingredients = new ArrayList<>();
    List<String> fileContents = new ArrayList<>();
    List<String> rawMeasurements = new ArrayList<>();
    List<String> measurements = new ArrayList<>();
    String pantryFilePath = "src/main/resources/archive/pantry/stock.json";
    String mealsFilePath = "src/main/resources/archive/meals/meals.json";
    String knownIngredientsFilePath = "src/main/resources/archive/meals/info/listOfKnownIngredients.json";
    File knownIngredientFile = new File(knownIngredientsFilePath);
    File pantryFile = new File(pantryFilePath);
    File mealsFile = new File(mealsFilePath);
    Scanner scanner = new Scanner(System.in);
    String value;
    CustomJsonObject.Ingredient pantry =
        gson.fromJson(getFileContent(pantryFile), CustomJsonObject.Ingredient.class);

    public HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public String fetchString(String uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).build();
        HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());
        final int statusCode = response.statusCode();
        if (statusCode != 200) throw new IOException("reponse status code not 200:" + statusCode);
        return response.body().trim();
    } // fetchString

    public Optional<ResponseObject.MealDBResult> searchMealDB(String q, String option_type) {
        try {
            String url = String.format("%s/%s.php?%s=%s", ENDPOINT_MEAL,
                                       option_type,"s",
                                       URLEncoder.encode(q, StandardCharsets.UTF_8));
            String json = fetchString(url);
            ResponseObject.MealDBResult result_meal =
                gson.fromJson(json, ResponseObject.MealDBResult.class);
            System.out.println("[ url ] " + url + "\n\n");
            return Optional.<ResponseObject.MealDBResult>ofNullable(result_meal);
        } catch (IllegalArgumentException | IOException | InterruptedException e) {
            System.out.println("Something went wrong with the mealDB api. " +
                               "Please try again or try a different search.");
            return Optional.<ResponseObject.MealDBResult>empty();
        }
    }

    public void processMealDBResult(ResponseObject.MealDBResult result) {
        if (result.meals != null) {
            meals.clear();
            for (ResponseObject.MealDBMeal meal : result.meals) meals.add(meal);
            System.out.printf("There are %s meals available.\n", meals.size());
            meals.stream().forEach(meal -> processMeal(meal));
            System.out.println("There is a possible "
                           + knownIngredientsHashSet.size()
                           + " unique ingredients to choose from.");
            writeToFile(mealsFilePath, gson.toJson(this.knownMealHashSet), "meals");
            writeToFile(knownIngredientsFilePath, gson.toJson(this.knownIngredientsHashSet), "ingredients");
        } else {
            System.out.println("Hm. I don't have anything for that. \n -> Perhaps try again?");
        }
    }

    private void processMeal(ResponseObject.MealDBMeal meal) {
        rawIngredients.add(meal.strIngredient1);
        rawIngredients.add(meal.strIngredient2);
        rawIngredients.add(meal.strIngredient3);
        rawIngredients.add(meal.strIngredient4);
        rawIngredients.add(meal.strIngredient5);
        rawIngredients.add(meal.strIngredient6);
        rawIngredients.add(meal.strIngredient7);
        rawIngredients.add(meal.strIngredient8);
        rawIngredients.add(meal.strIngredient9);
        rawIngredients.add(meal.strIngredient10);
        rawIngredients.add(meal.strIngredient11);
        rawIngredients.add(meal.strIngredient12);
        rawIngredients.add(meal.strIngredient13);
        rawIngredients.add(meal.strIngredient14);
        rawIngredients.add(meal.strIngredient15);
        rawIngredients.stream().forEach(ing -> {
                if (ing != null && ing.length() > 0) {
                    ingredients.add(ing);
                    knownIngredientsHashSet.add(ing.toLowerCase());
                }
            });
        ingredientArray = new String[ingredients.size()];
        for (int i = 0; i < ingredients.size() ; i++) ingredientArray[i] = ingredients.get(i);
        rawMeasurements.add(meal.strMeasure1);
        rawMeasurements.add(meal.strMeasure2);
        rawMeasurements.add(meal.strMeasure3);
        rawMeasurements.add(meal.strMeasure4);
        rawMeasurements.add(meal.strMeasure5);
        rawMeasurements.add(meal.strMeasure6);
        rawMeasurements.add(meal.strMeasure7);
        rawMeasurements.add(meal.strMeasure8);
        rawMeasurements.add(meal.strMeasure9);
        rawMeasurements.add(meal.strMeasure10);
        rawMeasurements.add(meal.strMeasure11);
        rawMeasurements.add(meal.strMeasure12);
        rawMeasurements.add(meal.strMeasure13);
        rawMeasurements.add(meal.strMeasure14);
        rawMeasurements.add(meal.strMeasure15);
        rawMeasurements.stream().forEach(mm -> {
                if (mm != null && mm.length() > 0) {
                    measurements.add(mm);
                }
            });
        measurementArray = new String[measurements.size()];
        for (int i = 0; i < measurements.size() ; i++) measurementArray[i] = measurements.get(i);
        addToCollection(meal.strMeal,
                        this.ingredientArray,
                        this.measurementArray,
                        meal.strDescription,
                        meal.strInstructions,
                        meal.strCategory,
                        meal.strArea);
        this.rawIngredients.clear();
        this.ingredients.clear();
        this.rawMeasurements.clear();
        this.measurements.clear();
    }

    private void addToCollection(String mealName,
                                 String[] ingredients,
                                 String[] measurements,
                                 String mealDescription,
                                 String mealInstructions,
                                 String mealCategory,
                                 String mealArea) {
        CustomJsonObject.Meal customMeal =
            new CustomJsonObject.Meal(mealName,
                                      ingredients,
                                      measurements,
                                      mealDescription,
                                      mealInstructions,
                                      mealCategory,
                                      mealArea);
        this.knownMealHashSet.add(customMeal);
    }

    private void writeToFile(String path, String content, String type) {
        try {
            FileWriter myWriter = new FileWriter(path);
            if (!content.startsWith("{")) {
                myWriter.write(String.format("{\"%s\":%s}", type, content));
            } else {
                myWriter.write(content);
            }
            myWriter.close();
            System.out.println(String.format("[ %s ] : populated.", path));
        } catch (IOException e) {
            System.out.println("An error occured.");
            e.printStackTrace();
        }
    }

    /**
     * getFileContent takes an input of type File.
     * Then, reads all the lines of the file and condenses the lines into a single
     * String variable.
     * @param file of type File
     * @return String
     **/
    private String getFileContent(File file) {
        List<String> lines;
        String content = "";
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            for (String line : lines) {
                content += line;
            }
            return content;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * userInterface permits the user to interact with the program to allow to custom modification
     * of inquiries formatted and shown in the CLI
     **/
    public void userInterface() {
        System.out.println("Welcome! How can I help you?");
        System.out.println("[0] options"
                           + "\n[1] search"
                           + "\n[2] pantry"
                           + "\n[3] list known meals"
                           + "\n[4] list known ingredients");

        this.value = scanner.nextLine();
        if (value.toLowerCase().equals("1")) {
            System.out.println("You chose search.\n Which meal are you looking for?");
            String mealString = scanner.nextLine();
            System.out.printf("Searching for %s\n", mealString);
            fillKnownIngredientList();
            fillKnownMealsSet();
            searchMealDB(mealString, "search")
                .ifPresent(result -> processMealDBResult(result));
            userInterface();
        }
        else if (value.toLowerCase().equals("0")) {
            System.out.println("You chose options.\n this path is not coded yet");
        }
        else if (value.toLowerCase().equals("2")) {
            System.out.println("Welcome to your pantry!");
            System.out.println("Would you like to [ ] ?"
                               + "\n[ 0 ] update pantry "
                               + "\n[ 1 ] show pantry contents "
                               + "\n[ 2 ] see what you can make "
                               + "\n[ 3 ] return");
            handlePantry();
            userInterface();
        } else if (value.toLowerCase().equals("3")) {
            System.out.println("You chose to list known meals. ");
            fillKnownMealsSet();
            printKnownMeals();
            userInterface();
        } else if (value.toLowerCase().equals("4")) {
            System.out.println("You chose to list known ingredients. ");
            fillKnownIngredientList();
            printKnownIngredients();
            userInterface();
        } else {
            return;
        }
    }

    public void handlePantry() {
        this.value = scanner.nextLine();
        if (value.equals("0")) {
            System.out.println("Choice: Update Pantry");
            updatePantry();
        } else if (value.equals("1")) {
            System.out.println("Choice: Show Pantry Contents");
            printPantryContents();
        } else if (value.equals("2")) {
            System.out.println("Choice: see what you can make");
        } else {
            System.out.println("Choice: return");
        }
    }

    public void updatePantry() {
        System.out.println("Would you like to [ ] ?"
                           + "\n[ 0 ] remove (an) item(s)"
                           + "\n[ 1 ] add (an) item(s)"
                           + "\n[ 2 ] audit pantry"
                           + "\n[ 3 ] go back an option set");
        this.value = scanner.nextLine();
        if (value.equals("0")) {
            System.out.println("You chose to remove an item. \n Which one would that be?");
            System.out.println("jk havent coded this part yet! ");
        } else if (value.equals("1")) {
            System.out.println("Your chose to add (an) item(s). \n Type your item(s)"
                               + " (*type \"exit\" when finished adding items*)");
            fillCurrentPantryList();
            addToPantry();
        } else if (value.equals("2")) {
            System.out.println("You chose to audit the pantry. Contact administrator. ");
        }
    }

    public CustomJsonObject.Ingredient findPantryFile() {
        File currentPantryFile = new File(pantryFilePath);
        CustomJsonObject.Ingredient currentPantry =
            gson.fromJson(getFileContent(pantryFile), CustomJsonObject.Ingredient.class);
        return pantry;
    }

    public void fillCurrentPantryList() {
        this.pantryItemList.clear();
        this.pantryItemList.addAll(Arrays.asList(pantry.getIngredients()));
    }

    public void printPantryContents() {
        CustomJsonObject.Ingredient pantry = findPantryFile();
        Arrays.asList(pantry.getIngredients()).forEach(System.out::println);
    }


    public void addToPantry() {
        this.value = this.scanner.nextLine();
        if (this.value.equals("exit")) {
            System.out.println("Would you like to see your inventory?"
                               + "\n[ 0 ] yes"
                               + "\n[ 1 ] no");
            this.value = scanner.nextLine();
            if (this.value.equals("0")) {
                this.pantryItemList.forEach(System.out::println);
                writeToFile(pantryFilePath, gson.toJson(this.pantryItemList), "ingredients");
            } else {
                writeToFile(pantryFilePath, gson.toJson(this.pantryItemList), "ingredients");
            }
        } else {
            this.pantryItemList.add(value);
            addToPantry();
        }
    }

    public void printKnownMeals() {
        File file = new File(mealsFilePath);
        CustomJsonObject.MealItems meals =
            gson.fromJson(getFileContent(file), CustomJsonObject.MealItems.class);
        for (CustomJsonObject.Meal meal : meals.meals) {
            System.out.println("Meal: "
                               + "\n    "
                               + meal.getMealName() + "\n");
        }
    }

    public void fillKnownMealsSet() {
        CustomJsonObject.MealItems meals =
            gson.fromJson(getFileContent(mealsFile), CustomJsonObject.MealItems.class);
        Arrays.asList(meals.meals).forEach(meal -> this.knownMealHashSet.add(meal));
    }

    public void fillKnownIngredientList() {
        CustomJsonObject.Ingredient knownIngs =
            gson.fromJson(getFileContent(knownIngredientFile), CustomJsonObject.Ingredient.class);
        Arrays.asList(knownIngs.getIngredients()).forEach(ing -> knownIngredientsHashSet.add(ing));
    }

    public void printKnownIngredients() {
        knownIngredientsHashSet.forEach(System.out::println);
    }



    public AppUtils() {
        super();
        this.userInterface();
    }
}

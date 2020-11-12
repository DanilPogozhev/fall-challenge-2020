package codingame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        Map<Integer, Recipe> recipes = new HashMap<>();

        while (true) {
            int actionCount = in.nextInt(); // the number of spells and recipes in play
            for (int i = 0; i < actionCount; i++) {
                Recipe recipe = new Recipe();
                recipe.setId(in.nextInt());

                String actionType = in.next(); // in the first league: BREW; later: CAST, OPPONENT_CAST, LEARN, BREW

                recipe.setIngCount_1(in.nextInt());
                recipe.setIngCount_2(in.nextInt());
                recipe.setIngCount_3(in.nextInt());
                recipe.setIngCount_4(in.nextInt());
                recipe.setPrice(in.nextInt());
                recipes.put(recipe.getId(), recipe);

                int tomeIndex = in.nextInt(); // in the first two leagues: always 0; later: the index in the tome if this is a tome spell, equal to the read-ahead tax
                int taxCount = in.nextInt(); // in the first two leagues: always 0; later: the amount of taxed tier-0 ingredients you gain from learning this spell
                boolean castable = in.nextInt() != 0; // in the first league: always 0; later: 1 if this is a castable player spell
                boolean repeatable = in.nextInt() != 0; // for the first two leagues: always 0; later: 1 if this is a repeatable player spell
            }

            Inventory inventory = new Inventory();

            for (int i = 0; i < 2; i++) {
                inventory.setIngCount_1(in.nextInt());
                inventory.setIngCount_2(in.nextInt());
                inventory.setIngCount_3(in.nextInt());
                inventory.setIngCount_4(in.nextInt());
                inventory.setScore(in.nextInt());
            }

            List<Recipe> affordableRecipes = UtilLogic.getAffordableRecipes(recipes, inventory);

            int id = UtilLogic.getMostUsefulRecipeId(affordableRecipes);
            System.err.println("RS: " + recipes.size());
            recipes.remove(id);
            System.err.println("RS: " + recipes.size());

            // in the first league: BREW <id> | WAIT; later: BREW <id> | CAST <id> [<times>] | LEARN <id> | REST | WAIT
            System.out.println("BREW " + id + " : " + id);
        }
    }
}

class UtilLogic {
    public static List<Recipe> getAffordableRecipes(Map<Integer, Recipe> recipes, Inventory inventory) {
        List<Recipe> affordableRecipes = new ArrayList<>();
        for (Recipe recipe : recipes.values()) {
            if (recipe.getIngCount_1() <= inventory.getIngCount_1()
                    && recipe.getIngCount_2() <= inventory.getIngCount_2()
                    && recipe.getIngCount_3() <= inventory.getIngCount_3()
                    && recipe.getIngCount_4() <= inventory.getIngCount_4()) {
                affordableRecipes.add(recipe);
            }
        }

        return affordableRecipes;
    }

    public static int getMostUsefulRecipeId(List<Recipe> recipes) {
        double minWeight = -100;
        int numberOfIngredients = -100;
        int id = 0;
        for (Recipe recipe : recipes) {
            double recipeWeight = recipe.getIngCount_1() * 0.2 + recipe.getIngCount_2() * 0.3
                    + recipe.getIngCount_3() * 0.5 + recipe.getIngCount_4();
            int recipeIngredientsNumber = recipe.getIngCount_1() + recipe.getIngCount_2()
                    + recipe.getIngCount_3() + recipe.getIngCount_4();
            if (recipeWeight >= minWeight && recipeIngredientsNumber >= numberOfIngredients) {
                System.err.println(recipeWeight + " " + recipeIngredientsNumber + " " + recipe.getId());

                minWeight = recipeWeight;
                numberOfIngredients = recipeIngredientsNumber;
                id = recipe.getId();
            }
        }

        return id;
    }
}

class Recipe {
    private int id;
    private int ingCount_1;
    private int ingCount_2;
    private int ingCount_3;
    private int ingCount_4;
    private int price;

    //<editor-fold desc="GettersSetters">
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIngCount_1() {
        return ingCount_1;
    }

    public void setIngCount_1(int ingCount_1) {
        this.ingCount_1 = ingCount_1;
    }

    public int getIngCount_2() {
        return ingCount_2;
    }

    public void setIngCount_2(int ingCount_2) {
        this.ingCount_2 = ingCount_2;
    }

    public int getIngCount_3() {
        return ingCount_3;
    }

    public void setIngCount_3(int ingCount_3) {
        this.ingCount_3 = ingCount_3;
    }

    public int getIngCount_4() {
        return ingCount_4;
    }

    public void setIngCount_4(int ingCount_4) {
        this.ingCount_4 = ingCount_4;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
    //</editor-fold>
}

class Inventory {
    private int ingCount_1;
    private int ingCount_2;
    private int ingCount_3;
    private int ingCount_4;
    private int score;

    //<editor-fold desc="GettersSetters">
    public int getIngCount_1() {
        return ingCount_1;
    }

    public void setIngCount_1(int ingCount_1) {
        this.ingCount_1 = ingCount_1;
    }

    public int getIngCount_2() {
        return ingCount_2;
    }

    public void setIngCount_2(int ingCount_2) {
        this.ingCount_2 = ingCount_2;
    }

    public int getIngCount_3() {
        return ingCount_3;
    }

    public void setIngCount_3(int ingCount_3) {
        this.ingCount_3 = ingCount_3;
    }

    public int getIngCount_4() {
        return ingCount_4;
    }

    public void setIngCount_4(int ingCount_4) {
        this.ingCount_4 = ingCount_4;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
    //</editor-fold>
}
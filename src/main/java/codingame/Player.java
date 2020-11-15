package codingame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        List<String> actionList = new ArrayList<>();
        Map<Integer, Recipe> recipes;
        Map<Integer, Recipe> myActions = new HashMap<>();
        Map<Integer, Recipe> opponentActions = new HashMap<>();
        Map<Integer, Recipe> learnMap = new HashMap<>();
        int targetId = 0;

        while (true) {
            //<editor-fold desc="Game setup">
            recipes = new HashMap<>();

            int actionCount = in.nextInt(); // the number of spells and recipes in play
            for (int i = 0; i < actionCount; i++) {
                Recipe recipe = new Recipe();
                recipe.setId(in.nextInt());
                recipe.setActionType(in.next());// in the first league: BREW; later: CAST, OPPONENT_CAST, LEARN, BREW
                recipe.setIngCount_1(in.nextInt());
                recipe.setIngCount_2(in.nextInt());
                recipe.setIngCount_3(in.nextInt());
                recipe.setIngCount_4(in.nextInt());
                recipe.setPrice(in.nextInt());
                recipe.setTomIndex(in.nextInt()); // in the first two leagues: always 0; later: the index in the tome if this is a tome spell, equal to the read-ahead tax
                recipe.setTaxCount(in.nextInt()); // in the first two leagues: always 0; later: the amount of taxed tier-0 ingredients you gain from learning this spell
                recipe.setCastable(in.nextInt() != 0); // in the first league: always 0; later: 1 if this is a castable player spell
                recipe.setRepeatable(in.nextInt() != 0); // for the first two leagues: always 0; later: 1 if this is a repeatable player spell

                if ("BREW".equals(recipe.getActionType())) {
                    recipes.put(recipe.getId(), recipe);
                } else if ("CAST".equals(recipe.getActionType())) {
                    myActions.put(recipe.getId(), recipe);
                } else if ("OPPONENT_CAST".equals(recipe.getActionType())) {
                    opponentActions.put(recipe.getId(), recipe);
                } else if ("LEARN".equals(recipe.getActionType())) {
                    learnMap.put(recipe.getId(), recipe);
                }
            }

            Recipe myInventory = new Recipe();
            Recipe enemyInventory = new Recipe();

            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    myInventory.setIngCount_1(in.nextInt());
                    myInventory.setIngCount_2(in.nextInt());
                    myInventory.setIngCount_3(in.nextInt());
                    myInventory.setIngCount_4(in.nextInt());
                    myInventory.setScore(in.nextInt());
                } else {
                    enemyInventory.setIngCount_1(in.nextInt());
                    enemyInventory.setIngCount_2(in.nextInt());
                    enemyInventory.setIngCount_3(in.nextInt());
                    enemyInventory.setIngCount_4(in.nextInt());
                    enemyInventory.setScore(in.nextInt());
                }
            }
            //</editor-fold>

            // POSSIBLE ACTIONS: in the first league:
            //      BREW <id> | WAIT
            //      BREW <id> | CAST <id> [<times>] | LEARN <id> | REST | WAIT

            if (actionList.isEmpty() || (targetId != 0 && recipes.get(targetId) == null)) {
                targetId = UtilLogic.getMostUsefulRecipeId(recipes.values(), myActions.values(), myInventory);
                Recipe recipe = recipes.get(targetId);

                boolean isAffordable = UtilLogic.isRecipeAffordable(recipe, myInventory);

                actionList = new ArrayList<>();

                if (isAffordable) {
                    String action = "BREW " + recipe.getId();
                    actionList.add(action);
                    recipes.remove(recipe.getId());
                } else {
                    UtilLogic.createActionList(myActions, myInventory, recipe, actionList);
                }
            } else if (myInventory.size() > 5) {
                int id = 0;
                for (Recipe recipe : recipes.values()) {
                    id = recipe.getId();
                    if (UtilLogic.isRecipeAffordable(recipe, myInventory)) {
                        String action = "BREW " + id;
                        actionList = new ArrayList<>();
                        actionList.add(action);
                    }
                }

                if (id != 0) {
                    recipes.remove(id);
                }
            }

            if (actionList.isEmpty() || !actionList.get(actionList.size() - 1).contains("REST")) {
                actionList.add("REST");
            }

            System.out.println(actionList.get(0));
            actionList.remove(0);
        }
    }
}

class UtilLogic {

    public static int getMostUsefulRecipeId(Collection<Recipe> recipes, Collection<Recipe> casts, Recipe inventory) {
        int id = 0;
        double minWeight = 100;
        double prevRatio = 0;

        for (Recipe recipe : recipes) {
            double recipeWeight = Math.abs(0.25 * recipe.getIngCount_1() + 0.5 * recipe.getIngCount_2()
                    + 0.75 * recipe.getIngCount_3() + recipe.getIngCount_4());
            double ratio = recipe.getPrice() / recipeWeight;

            if (recipeWeight < minWeight || (recipeWeight == minWeight && ratio > prevRatio)) {
                minWeight = recipeWeight;
                prevRatio = ratio;
                id = recipe.getId();
            }
        }

        return id;
    }

    public static List<Recipe> getAffordableCasts(Collection<Recipe> myActions, Recipe inventory) {
        List<Recipe> affordableRecipes = new ArrayList<>();

        for (Recipe cast : myActions) {
            if (cast.isCastable() && (isCastFree(cast) || isRecipeAffordable(cast, inventory))) {
                affordableRecipes.add(cast);
            }
        }

        return affordableRecipes;
    }

    public static boolean isRecipeAffordable(Recipe recipe, Recipe inventory) {
        return (recipe.getIngCount_1() + inventory.getIngCount_1() >= 0)
                && (recipe.getIngCount_2() + inventory.getIngCount_2() >= 0)
                && (recipe.getIngCount_3() + inventory.getIngCount_3() >= 0)
                && (recipe.getIngCount_4() + inventory.getIngCount_4() >= 0);
    }

    private static boolean isCastFree(Recipe recipe) {
        return recipe.getIngCount_1() >= 0
                && recipe.getIngCount_2() >= 0
                && recipe.getIngCount_3() >= 0
                && recipe.getIngCount_4() >= 0;
    }

    public static List<String> createActionList(Map<Integer, Recipe> myActions, Recipe myInventory, Recipe recipe, List<String> actionList) {
        List<Recipe> affordableCasts = getAffordableCasts(myActions.values(), myInventory);
        int castId = getUsefulCast(affordableCasts, myInventory, recipe);


        if (castId == 0) {
            return actionList;
        } else {
            actionList.add("CAST " + castId);
            Recipe newInventory = sum(myActions.get(castId), myInventory);
            myActions.remove(castId);
            if (isRecipeAffordable(recipe, newInventory)) {
                return actionList;
            } else {
                if (myActions.values().isEmpty()) {
                    actionList.add("REST");
                } else {
                    createActionList(myActions, newInventory, recipe, actionList);
                }
            }
        }

        return actionList;
    }

    private static int getUsefulCast(List<Recipe> affordableCasts, Recipe myInventory, Recipe recipe) {
        int id = 0;

        for (Recipe cast : affordableCasts) {
            String castType = analyseCast(cast);

            boolean firstNotEnough = myInventory.getIngCount_1() + recipe.getIngCount_1() < 0 && recipe.getIngCount_1() < 0;
            boolean secondNotEnough = myInventory.getIngCount_2() + recipe.getIngCount_2() < 0 && recipe.getIngCount_2() < 0;
            boolean thirdNotEnough = myInventory.getIngCount_3() + recipe.getIngCount_3() < 0 && recipe.getIngCount_3() < 0;
            boolean fourthNotEnough = myInventory.getIngCount_4() + recipe.getIngCount_4() < 0 && recipe.getIngCount_4() < 0;

            if (firstNotEnough && myInventory.getIngCount_1() < 5) {
                if (castType.contains("P_FIRST")) {
                    return cast.getId();
                }
            }

            if (secondNotEnough) {
                if (castType.contains("P_SECOND")) {
                    return cast.getId();
                }

                if (castType.contains("P_FIRST") && myInventory.getIngCount_1() == 0) {
                    return cast.getId();
                }
            }

            if (thirdNotEnough) {
                if (castType.contains("P_THIRD")) {
                    return cast.getId();
                }

                if (castType.contains("P_SECOND")) {
                    return cast.getId();
                }

                if (castType.contains("P_FIRST") && myInventory.getIngCount_1() == 0) {
                    return cast.getId();
                }
            }

            if (fourthNotEnough) {
                if (castType.contains("P_FOURTH")) {
                    return cast.getId();
                }

                if (castType.contains("P_THIRD")) {
                    return cast.getId();
                }

                if (castType.contains("P_SECOND")) {
                    return cast.getId();
                }

                if (castType.contains("P_FIRST") && myInventory.getIngCount_1() == 0) {
                    return cast.getId();
                }
            }
        }

        return id;
    }

    private static String analyseCast(Recipe cast) {
        String castType = "";

        if (cast.getIngCount_1() < 0) {
            castType += "M_FIRST ";
        } else if (cast.getIngCount_1() > 0) {
            castType += "P_FIRST ";
        }

        if (cast.getIngCount_2() < 0) {
            castType += "M_SECOND ";
        } else if (cast.getIngCount_2() > 0) {
            castType += "P_SECOND ";
        }

        if (cast.getIngCount_3() < 0) {
            castType += "M_THIRD ";
        } else if (cast.getIngCount_3() > 0) {
            castType += "P_THIRD ";
        }

        if (cast.getIngCount_4() < 0) {
            castType += "M_FOURTH ";
        } else if (cast.getIngCount_4() > 0) {
            castType += "P_FOURTH ";
        }

        return castType.trim();
    }

    private static Recipe sum(Recipe cast, Recipe myInventory) {
        Recipe recipe = new Recipe();
        recipe.setIngCount_1(cast.getIngCount_1() + myInventory.getIngCount_1());
        recipe.setIngCount_2(cast.getIngCount_2() + myInventory.getIngCount_2());
        recipe.setIngCount_3(cast.getIngCount_3() + myInventory.getIngCount_3());
        recipe.setIngCount_4(cast.getIngCount_3() + myInventory.getIngCount_4());
        return recipe;
    }
}

class Recipe {
    private int id;
    private int ingCount_1;
    private int ingCount_2;
    private int ingCount_3;
    private int ingCount_4;
    private int price;
    private String actionType;
    private int tomIndex;
    private int taxCount;
    private boolean castable;
    private boolean repeatable;

    private int score;
    private String castType;

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

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public int getTomIndex() {
        return tomIndex;
    }

    public void setTomIndex(int tomIndex) {
        this.tomIndex = tomIndex;
    }

    public int getTaxCount() {
        return taxCount;
    }

    public void setTaxCount(int taxCount) {
        this.taxCount = taxCount;
    }

    public boolean isCastable() {
        return castable;
    }

    public void setCastable(boolean castable) {
        this.castable = castable;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getCastType() {
        return castType;
    }

    public void setCastType(String castType) {
        this.castType = castType;
    }

    public String printData() {
        return this.getId() + " : " + getIngCount_1() + " " + getIngCount_2() + " " + getIngCount_3() + " " + getIngCount_4();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Recipe recipe = (Recipe) o;

        return getId() == recipe.getId();
    }

    @Override
    public int hashCode() {
        return getId();
    }

    public int size() {
        return getIngCount_2() + getIngCount_3() + getIngCount_4();
    }
    //</editor-fold>
}
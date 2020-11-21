import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Map<Integer, Integer[]> casts;
        Map<Integer, Integer[]> opponentCasts;
        Map<Integer, Integer[]> studies;
        Map<Integer, Integer[]> brews;

        while (true) {
            casts = new HashMap<>();
            opponentCasts = new HashMap<>();
            studies = new HashMap<>();
            brews = new HashMap<>();
            int actionCount = in.nextInt();
            long timeStart = System.nanoTime();
            for (int i = 0; i < actionCount; i++) {
                int recipeId = in.nextInt();

                int cast;
                String castType = in.next();
                if (castType.equals("CAST")) {
                    cast = 0;
                } else if (castType.equals("OPPONENT_CAST")) {
                    cast = 1;
                } else if (castType.equals("LEARN")) {
                    cast = 2;
                } else {
                    cast = 3;
                }

                Integer[] array = new Integer[]{recipeId, cast, in.nextInt(), in.nextInt(), in.nextInt(),
                        in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};

                if (cast == 0) {
                    casts.put(array[0], array);
                } else if (cast == 1) {
                    opponentCasts.put(array[0], array);
                } else if (cast == 2) {
                    studies.put(array[0], array);
                } else {
                    brews.put(array[0], array);
                }
            }

            System.err.println("CASTS: " + casts.size());
            System.err.println("OPPCS: " + opponentCasts.size());
            System.err.println("STUDY: " + studies.size());
            System.err.println("BREWS: " + brews.size());

            int[] myBag;
            int myScore;
            int[] opponentBag;
            int opponentsScore;
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    myBag = new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
                    myScore = in.nextInt();
                } else {
                    opponentBag = new int[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
                    opponentsScore = in.nextInt();
                }
            }

            long timeSpentOnInputReading = (System.nanoTime() - timeStart) / 1_000_000;
            System.err.println("TS: " + timeSpentOnInputReading);
        }
    }
}
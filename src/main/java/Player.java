import java.util.Scanner;

class Player {

    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);

        while (true) {

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

                int[] array = new int[]{recipeId, cast, in.nextInt(), in.nextInt(), in.nextInt(),
                        in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
            }

            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    int[] array = {-1, -1, in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
                } else {
                    int[] array = {-2, -2, in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
                }
            }

            long timeSpentOnInputReading = (System.nanoTime() - timeStart) / 1_000_000;
            System.err.println("TS: " + timeSpentOnInputReading);
        }
    }
}
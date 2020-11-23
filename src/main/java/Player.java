import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

class Player {
    public static void main(String args[]) {
        Scanner in = new Scanner(System.in);
        Map<Integer, Integer[]> casts;
        Map<Integer, Integer[]> opponentCasts;
        Map<Integer, Integer[]> studies;
        Map<Integer, Integer[]> brews;
        Set<Integer> exhausted = new HashSet<>();
        Map<Integer, Integer> idMapping = new HashMap<>();
        String action = "";
        String previousGoal = "";
        String castsUsual = "7879808182838485";
        String prevAction = "";

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
                    if (prevAction.contains("LEARN") && !castsUsual.contains("" + array[0])) {
                        String[] split = prevAction.split(" ");
                        idMapping.putIfAbsent(Integer.valueOf(split[1]), array[0]);
                        castsUsual += "" + array[0];
                    }
                    casts.put(array[0], array);
                } else if (cast == 1) {
                    opponentCasts.put(array[0], array);
                } else if (cast == 2) {
                    if (!exhausted.contains(array[0])) {
                        array[6] = studies.size();
                        studies.put(array[0], array);
                    }
                } else {
                    brews.put(array[0], array);
                }
            }

            Integer[] myBag = null;
            int myScore;
            Integer[] opponentBag = null;
            int opponentsScore;
            for (int i = 0; i < 2; i++) {
                if (i == 0) {
                    myBag = new Integer[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
                    myScore = in.nextInt();
                } else {
                    opponentBag = new Integer[]{in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt()};
                    opponentsScore = in.nextInt();
                }
            }

            long bfsStartTime = System.nanoTime();
            long timeSpentOnInputReading = (bfsStartTime - timeStart) / 1_000_000;

            List<Vertex> vertices = new ArrayList<>();
            vertices = generateVerticesList(casts.values(), vertices);
            vertices = generateVerticesList(studies.values(), vertices);

            BreadthFirstSearch bfs = new BreadthFirstSearch(vertices, brews.values(), bfsStartTime, timeSpentOnInputReading);

            Node node = new Node();
            node.color = "WHITE";
            node.parent = null;
            node.position = 0;
            node.element_0 = myBag[0];
            node.element_1 = myBag[1];
            node.element_2 = myBag[2];
            node.element_3 = myBag[3];

            String newAction = bfs.bfs(node);

            if (action.equals("") || (!previousGoal.equals("") && !newAction.endsWith(previousGoal))) {
                if (action.equals("REST")) {
                    if (newAction.trim().startsWith("LEARN")) {
                        action = newAction.trim() + " REST";
                    } else if (action.equals(newAction)) {
                        action = newAction;
                    } else {
                        action = "REST " + newAction;
                    }
                } else {
                    action = newAction;
                }
                System.err.println(action);
                previousGoal = action.trim().equals("REST") ? "REST" : action.substring(action.indexOf("BREW"));
            }

            action = action.trim();

            if (action.startsWith("C")) {

                String currentAction = action.substring(0, 7);
                String[] split = currentAction.split(" ");

                Integer newId = idMapping.get(Integer.valueOf(split[1]));

                if (newId != null) {
                    System.out.println("CAST " + newId);
                } else {
                    System.out.println(currentAction);
                }

                action = action.substring(8);
                prevAction = "cast";
            } else if (action.startsWith("L")) {
                String[] split = action.split(" ");
                String learnAction = split[0] + " " + split[1];

                prevAction = learnAction;
                exhausted.add(Integer.valueOf(split[1]));
                action = action.substring(learnAction.length());

                System.out.println(learnAction);
            } else if (action.startsWith("R")) {
                System.out.println(action.substring(0, 4));
                if (action.length() < 5) {
                    action = "";
                } else {
                    action = action.substring(5);
                }
                prevAction = "rest";
            } else if (action.startsWith("B")) {
                System.out.println(action.substring(0, 7));
                prevAction = "brew";
                action = "REST";
            } else {
                System.err.println("OUT OF TIME");
                System.out.println("REST");
                prevAction = "rest";
            }
        }
    }

    private static List<Vertex> generateVerticesList(Collection<Integer[]> gameElements, List<Vertex> vertices) {
        if (vertices == null) {
            vertices = new ArrayList<>();
        }

        for (Integer[] element : gameElements) {
            Vertex vertex = new Vertex(element[0], element[1], element[2], element[3], element[4], element[5]);

            if (element[1] == 2) {
                vertex.innerId = element[6];
            }

            vertices.add(vertex);
        }

        return vertices;
    }
}

class BreadthFirstSearch {
    private Queue<Node> queue;
    private List<Vertex> vertices;
    private Collection<Integer[]> goals;

    private long timeSpent;
    private long bfsStart;

    public BreadthFirstSearch(List<Vertex> vertices, Collection<Integer[]> goals, long bfsStart, long timeSpent) {
        this.vertices = vertices;
        this.goals = goals;
        this.timeSpent = timeSpent;
        this.bfsStart = bfsStart;
        queue = new LinkedList<>();
    }

    public String bfs(Node rootNode) {
        rootNode.color = "GRAY";
        if (rootNode.parent != null) {
            rootNode.position = rootNode.parent.position + 1;
        } else {
            rootNode.position = 0;
            rootNode.parent = null;
        }

        queue.add(rootNode);

        int numberOfSteps = Integer.MAX_VALUE;
        String action = "REST";

        while (!queue.isEmpty()) {
            Node node = queue.poll();
            List<Node> roots = generateRoots(node, timeSpent, bfsStart);

            boolean breakTime = 50 - timeSpent - (System.nanoTime() - bfsStart) / 1_000_000 <= 5;

            if (breakTime) {
                return action;
            }

            for (Node child : roots) {
                breakTime = 50 - timeSpent - (System.nanoTime() - bfsStart) / 1_000_000 <= 3;

                if (breakTime) {
                    break;
                }

                if (child.position > 12) {
                    continue;
                }

                if (child.color.equals("WHITE")) {
                    child.color = "GRAY";

                    Integer nodeApplicable = isNodeApplicable(child, goals);
                    if (nodeApplicable != -1) {
                        if (child.position + 1 < numberOfSteps) {
                            numberOfSteps = child.position + 1;
                            action = child.data + "BREW " + nodeApplicable;
                        }
                    }

                    queue.add(child);
                }
            }
            node.color = "BLACK";
        }

        return action;
    }

    private List<Node> generateRoots(Node baseNode, long timeSpent, long bfsStart) {
        Node parent = baseNode.parent;
        List<Node> roots = new ArrayList<>();

        List<Vertex> copyVertices = new ArrayList<>();
        copyVertices.addAll(vertices);


        if (50 - timeSpent - (System.nanoTime() - bfsStart) / 1_000_000 <= 3) {
            return roots;
        }

        for (Vertex vertex : copyVertices) {
            if (50 - timeSpent - (System.nanoTime() - bfsStart) / 1_000_000 <= 3) {
                break;
            }

            String data = baseNode.data;

            if (data.contains("LEARN " + vertex.id) || (vertex.innerId != -1 && data.contains("CAST " + vertex.id))) {
                continue;
            }

            int result_0 = baseNode.element_0 + vertex.element_0;
            int result_1 = baseNode.element_1 + vertex.element_1;
            int result_2 = baseNode.element_2 + vertex.element_2;
            int result_3 = baseNode.element_3 + vertex.element_3;

            if (parent != null && parent.element_0 == result_0 && parent.element_1 == result_1
                    && parent.element_2 == result_2 && parent.element_3 == result_3) {
                continue;
            }

            if (result_0 + result_1 + result_2 + result_3 <= 10
                    && result_0 >= 0 && result_1 >= 0 && result_2 >= 0 && result_3 >= 0) {
                Node node = new Node();
                node.position = baseNode.position + 1;

                if (50 - timeSpent - (System.nanoTime() - bfsStart) / 1_000_000 <= 3) {
                    break;
                }

                if (vertex.castType == 0) {
                    int lastRestIndex = data.lastIndexOf("REST");
                    if (data.contains("" + vertex.id)) {
                        if (data.contains("REST")) {
                            int firstRestIndex = data.indexOf("REST");
                            if (firstRestIndex == lastRestIndex) {
                                if (!data.substring(0, firstRestIndex).contains("" + vertex.id)) {
                                    data = data.substring(0, lastRestIndex - 1) + " CAST " + vertex.id + " REST" + data.substring(lastRestIndex + 4);
                                } else {
                                    if (data.substring(firstRestIndex).contains("" + vertex.id)) {
                                        data += "REST CAST " + vertex.id;
                                        node.position += 1;
                                    } else {
                                        data += "CAST " + vertex.id;
                                    }
                                }
                            } else {
                                if (!data.substring(firstRestIndex, lastRestIndex).contains("" + vertex.id)) {
                                    data = data.substring(0, lastRestIndex - 1) + " CAST " + vertex.id + " REST" + data.substring(lastRestIndex + 4);
                                } else {
                                    if (data.substring(firstRestIndex).contains("" + vertex.id)) {
                                        data += "REST CAST " + vertex.id;
                                        node.position += 1;
                                    } else {
                                        data += "CAST " + vertex.id;
                                    }
                                }
                            }
                        } else {
                            data += "REST CAST " + vertex.id;
                            node.position += 1;
                        }
                    } else {
                        if (data.contains("REST")) {
                            data = data.substring(0, lastRestIndex - 1) + " CAST " + vertex.id + " REST" + data.substring(lastRestIndex + 4);
                        } else {
                            data += "CAST " + vertex.id;
                        }
                    }
                } else {
                    if (!data.contains("CAST " + vertex.id)) {
                        String generatedChain = "";
                        if (vertex.innerId != 0) {
                            generatedChain = generateLearnChain(vertex.innerId);
                        }

                        data = generatedChain + " LEARN " + vertex.id + " CAST " + vertex.id + " " + data;

                        node.position += vertex.innerId + 1;
                    }
                }

                node.data = data + " ";
                node.color = "WHITE";
                node.parent = baseNode;
                node.element_0 = result_0;
                node.element_1 = result_1;
                node.element_2 = result_2;
                node.element_3 = result_3;
                roots.add(node);
            }
        }

        return roots;
    }

    private String generateLearnChain(int innerId) {
        Map<Integer, Integer> map = new HashMap<>();

        for (Vertex vertex : vertices) {
            if (vertex.innerId != -1) {
                map.put(vertex.innerId, vertex.id);
            }
        }

        String result = "";
        int counter = 0;

        while (counter != innerId) {
            result += "LEARN " + map.get(counter) + " ";
            counter++;
        }

        return result.trim();
    }

    private Integer isNodeApplicable(Node node, Collection<Integer[]> goals) {
        for (Integer[] goal : goals) {

            int result_0 = node.element_0 + goal[2];
            int result_1 = node.element_1 + goal[3];
            int result_2 = node.element_2 + goal[4];
            int result_3 = node.element_3 + goal[5];


            if (result_0 + result_1 + result_2 + result_3 <= 10
                    && result_0 >= 0
                    && result_1 >= 0
                    && result_2 >= 0
                    && result_3 >= 0) {
                return goal[0];
            }
        }

        return -1;
    }
}

class Node {
    Node parent;
    String color;
    int position;
    String data = "";

    int element_0;
    int element_1;
    int element_2;
    int element_3;
}

class Vertex {
    int id;
    int castType;
    int element_0;
    int element_1;
    int element_2;
    int element_3;
    int innerId = -1;

    public Vertex(int id, int castType, int element_0, int element_1, int element_2, int element_3) {
        this.id = id;
        this.castType = castType;
        this.element_0 = element_0;
        this.element_1 = element_1;
        this.element_2 = element_2;
        this.element_3 = element_3;
    }
}
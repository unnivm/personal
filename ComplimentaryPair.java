/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author Unni Vemanchery Mana
 */
public class ComplimentaryPair {

    public static void main(String[] arg) {
        int[] arr = {2, 5, -1, 6, 10, -2};
        int k = 5;

        System.out.println(calculateComplimentaryPairs(arr, k));
    }

    /**
     * This method calculates the complimentary pair by traversing map
     * traversing map takes O(N) time.
     *
     * time Complexity: O(N)
     * space Complexity: O(1)
     *
     * @param arr
     * @param K
     * @return
     */
    public static int calculateComplimentaryPairs(int[] arr, int K) {

        int count = 0;
        HashMap<Integer, Integer> map = new HashMap<>();

        // HashMap has constant access time
        for (int i : arr) {
            if (map.get(i) == null) {
                map.put(i, 1);
            } else {
                map.put(i, map.get(i) + 1);
            }
        }

        Set<Integer> keys = map.keySet();
        for (Integer key : keys) {
            int C = K - key;
            if (map.containsKey(C)) {
                count += map.get(key) * map.get(C);
            }
        }

        return count;
    }

}

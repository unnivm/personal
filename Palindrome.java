/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Unni Vemanchery Mana
 */
public class Palindrome {

    public static void main(String[] arg) {

        boolean palindrome = isPalindrome("121");

        if (palindrome) {
            System.out.println("Palindrome");
        } else {
            System.out.println("Not palindrome");
        }

    }

    /**
     * The following algorithm checks for a string is a
     * palindrome or not.
     *
     * Complexity: O(log n)
     *
     * @param s
     * @return
     */
    public static boolean isPalindrome(String s) {

        if(s == null) return false;

        s = s.trim();
        if(s.isEmpty() ) {
            return false;
        }

        int n = s.length();
        for (int i = 0; i < (n / 2); ++i) {
            if (s.charAt(i) != s.charAt(n - i - 1)) {
                return false;
            }
        }
        return true;
    }

}

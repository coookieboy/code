public class P125 {
    public static void main(String[] args) {

    }

    public boolean isPalindrome(String s) {
        int left = 0;
        int right = s.length() - 1;
        while (left < right) {
            char l = s.charAt(left);
            if (l < 'A' || l > 'z') {
                left++;
                continue;
            }
            char r = s.charAt(right);
            if (r < 'A' || r > 'z') {
                right--;
                continue;
            }
            if (!String.valueOf(l).equalsIgnoreCase(String.valueOf(r))) {
                return false;
            }
            left++;
            right--;
        }
        return true;
    }
}

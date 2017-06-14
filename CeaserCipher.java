import java.util.Scanner;

public class CeaserCipher {

	public static void main(String[] args) {

		Scanner in = new Scanner(System.in);

		int N = in.nextInt();
		String s = in.next();
		int k = in.nextInt();

		StringBuilder cypher = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {

			int c = s.charAt(i);

			if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {

				if (c >= 65 && c <= 90) { // upper case
					int c1 = getCipher(65, 90, c, k);
					cypher.append((char) c1 + "");
				} else if (c >= 97 && c <= 122) { // lower case
					int c1 = getCipher(97, 122, c, k);
					cypher.append((char) c1 + "");
				}

			} else {
				cypher.append(s.charAt(i) + "");
			}
		}

		in.close();

		if (N == cypher.length())
			System.out.println(cypher.toString());
	}

	private static int getCipher(int start, int end, int current, int k) {
		int position = current;
		for (int i = 0; i < k; i++) {
			if (position >= end)
				position = start - 1;
			position++;
		}

		return position;
	}

}

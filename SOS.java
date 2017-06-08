import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SOS {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Scanner in = new Scanner(System.in);
		String S = in.next();

		List<String> strings = new ArrayList<String>();
		int index = 0;

		while (index < S.length()) {
			strings.add(S.substring(index, Math.min(index + 3, S.length())));
			index += 3;
		}

		int cnt = 0;
		for (String s1 : strings) {

			if (s1.charAt(0) != 'S')
				cnt++;
			if (s1.charAt(1) != 'O')
				cnt++;
			if (s1.charAt(2) != 'S')
				cnt++;
		}

		in.close();
		System.out.println(cnt);

	}

}

import java.util.HashMap;


public class Test {
	static HashMap<Integer, String> onesMap = new HashMap<>();
	static HashMap<Integer, String> teensMap = new HashMap<>();
	static HashMap<Integer, String> tensMap = new HashMap<>();
	
	public static void main(String[] args) {
		onesMap.put(0, " Zero");
		onesMap.put(1, " One");
		onesMap.put(2, " Two");
		onesMap.put(3, " Three");
		onesMap.put(4, " Four");
		onesMap.put(5, " Five");
		onesMap.put(6, " Six");
		onesMap.put(7, " Seven");
		onesMap.put(8, " Eight");
		onesMap.put(9, " Nine");
		
		tensMap.put(0, "");
		tensMap.put(1, " Ten");
		tensMap.put(2, " Twenty");
		tensMap.put(3, " Thirty");
		tensMap.put(4, " Forty");
		tensMap.put(5, " Fifty");
		tensMap.put(6, " Sixty");
		tensMap.put(7, " Seventy");
		tensMap.put(8, " Eighty");
		tensMap.put(9, " Ninety");

		teensMap.put(11, " Eleven");
		teensMap.put(12, " Twelve");
		teensMap.put(13, " Thirteen");
		teensMap.put(14, " Fourteen");
		teensMap.put(15, " Fifteen");
		teensMap.put(16, " Sixteen");
		teensMap.put(17, " Seventeen");
		teensMap.put(18, " Eighteen");
		teensMap.put(19, " Nineteen");
		
		System.out.println(parseCheck(792));
	}

	public static String parseCheck(int num) {
		String result = "";
		for(int i = 100; i > 0; i/=10) {
			int e = (num % (i*10)) / i;
			if(i == 1) {
				result += onesMap.get(e);
			} else if (i == 10) {
				if(num % (i*10) < 20 && num % (i*10) > 10) {
					result += teensMap.get(num % (i*10));
					i = 0;
				} else {
					result += tensMap.get(e);
				}
			} else {
				result += onesMap.get(e) + " hundred";
			}
		}
		return result;
	}
}

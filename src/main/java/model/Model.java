package model;

public class Model {

	public Integer parseInteger(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	public boolean isPrime(int number) {
		if (number < 2) {
			return false;
		}
		if (number == 2) {
			return true;
		}
		if (number % 2 == 0) {
			return false;
		}
		for (int divisor = 3; divisor * divisor <= number; divisor += 2) {
			if (number % divisor == 0) {
				return false;
			}
		}
		return true;
	}
}

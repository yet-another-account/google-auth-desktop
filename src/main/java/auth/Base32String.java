package auth;

import java.util.HashMap;

/**
 * Encodes arbitrary byte arrays as case-insensitive base-32 strings
 * 
 * @author eukaryote
 * @author sweis@google.com (Steve Weis)
 * @author Neal Gafter
 */
public class Base32String {

	public static final Base32String RFC4668 = new Base32String("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");

	// 32 alpha-numeric characters. Excluding 0, 1, O, and I
	private char[] digits;
	private int mask;
	private int shift;
	private HashMap<Character, Integer> charmap;

	private static final String SEPARATOR = "-";

	public Base32String(String alphabet) {
		digits = alphabet.toCharArray();
		mask = digits.length - 1;
		shift = Integer.numberOfTrailingZeros(digits.length);
		charmap = new HashMap<Character, Integer>();
		
		for (int i = 0; i < digits.length; i++) {
			charmap.put(digits[i], i);
		}
	}

	public byte[] decode(String encoded) throws DecodingException {
		// Remove whitespace and separators
		encoded = encoded.trim().replaceAll(SEPARATOR, "").replaceAll(" ", "");
		// Canonicalize to all upper case
		encoded = encoded.toUpperCase();
		if (encoded.length() == 0) {
			return new byte[0];
		}
		int encodedLength = encoded.length();
		int outLength = encodedLength * shift / 8;
		byte[] result = new byte[outLength];
		int buffer = 0;
		int next = 0;
		int bitsLeft = 0;
		for (char c : encoded.toCharArray()) {
			if (!charmap.containsKey(c)) {
				throw new DecodingException("Illegal character: " + c);
			}
			buffer <<= shift;
			buffer |= charmap.get(c) & mask;
			bitsLeft += shift;
			if (bitsLeft >= 8) {
				result[next++] = (byte) (buffer >> (bitsLeft - 8));
				bitsLeft -= 8;
			}
		}
		// We'll ignore leftover bits for now.
		//
		// if (next != outLength || bitsLeft >= SHIFT) {
		// throw new DecodingException("Bits left: " + bitsLeft);
		// }
		return result;
	}

	public String encode(byte[] data) {
		if (data.length == 0) {
			return "";
		}

		// SHIFT is the number of bits per output character, so the length of
		// the
		// output is the length of the input multiplied by 8/SHIFT, rounded up.
		if (data.length >= (1 << 28)) {
			// The computation below will fail, so don't do it.
			throw new IllegalArgumentException();
		}

		int outputLength = (data.length * 8 + shift - 1) / shift;
		StringBuilder result = new StringBuilder(outputLength);

		int buffer = data[0];
		int next = 1;
		int bitsLeft = 8;
		while (bitsLeft > 0 || next < data.length) {
			if (bitsLeft < shift) {
				if (next < data.length) {
					buffer <<= 8;
					buffer |= (data[next++] & 0xff);
					bitsLeft += 8;
				} else {
					int pad = shift - bitsLeft;
					buffer <<= pad;
					bitsLeft += pad;
				}
			}
			int index = mask & (buffer >> (bitsLeft - shift));
			bitsLeft -= shift;
			result.append(digits[index]);
		}
		return result.toString();
	}

	static class DecodingException extends Exception {
		private static final long serialVersionUID = -1L;

		public DecodingException(String message) {
			super(message);
		}
	}
}

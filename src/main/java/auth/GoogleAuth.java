package auth;

import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 
 * A wrapper for the underlying RFC4226 implementation. 
 * 
 * @author eukaryote
 *
 */
public class GoogleAuth {
	private String secret;
	
	public GoogleAuth(String secret) {
		this.secret = secret;
	}
	
	public String computePin() {
		if (secret == null || secret.isEmpty()) {
			throw new IllegalArgumentException("secret cannot be null or empty");
		}
		try {
			final byte[] keyBytes = Base32String.RFC4668.decode(secret);
			
			Mac mac = Mac.getInstance("HMACSHA1");
			mac.init(new SecretKeySpec(keyBytes, ""));
			PasscodeGenerator pcg = new PasscodeGenerator(mac);
			return pcg.generateTimeoutCode();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		} catch (Base32String.DecodingException e) {
			throw new RuntimeException(e);
		}
	}
}

package auth.manager;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class TestAuthManager {
	@Test
	public void testAuth() throws Exception {
		new File("test.json").delete();
		AuthManager am = new AuthManager(new File("test.json"));
		am.addToken("appname", "token");
		am.encrypt(null, "abc123");
		
		assertEquals("token", am.getToken(0, "abc123"));
	}
}

package auth.manager;

import java.io.File;

import org.junit.Test;

public class TestAuthManager {
	@Test
	public void testAuth() throws Exception {
		new File("test.json").delete();
		AuthManager am = new AuthManager(new File("test.json"));
		am.addToken("appname", "token");
		am.save();
	}
}

package auth.manager;

import java.util.LinkedList;

public class TokenDB {
	TokenDB() {}
	
	boolean isEncrypted;
	String initvector;
	String passwordhash;
	LinkedList<TokenEntry> tokens;
	
	
	public static class TokenEntry {
		// no-args construstor
		TokenEntry() {}
		
		public TokenEntry(String appname, String token) {
			this.appname = appname;
			this.token = token;
		}
		
		String appname;
		String token;
	}
}

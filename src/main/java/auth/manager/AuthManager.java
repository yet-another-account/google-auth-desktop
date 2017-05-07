package auth.manager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import auth.manager.TokenDB.TokenEntry;

public class AuthManager {
	TokenDB tokens;
	File authfile;
	
	public AuthManager(File authfile) throws IOException {
		if(!authfile.exists())
			createNew(authfile);
		
		this.authfile = authfile;
	}
	
	public boolean isEncrypted() {
		return false;
	}
	
	public void addToken(String appname, String token) {
		tokens.tokens.add(new TokenEntry(appname, token));
	}
	
	protected void createNew(File authfile) throws IOException {
		tokens = new TokenDB();
		tokens.isEncrypted = false;
		tokens.tokens = new LinkedList<TokenEntry>();
		
		
	}
	
	protected void getTokens(File authfile) throws IOException {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		tokens = gson.fromJson(FileUtils.readFileToString(authfile, Charset.forName("UTF-8")), TokenDB.class);
	}
	
	public void save() throws IOException {
		Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.create();
		FileUtils.write(authfile, gson.toJson(tokens), Charset.forName("UTF-8"));
	}
}

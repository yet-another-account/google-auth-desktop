package auth.manager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.LinkedList;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.crypto.random.CryptoRandom;
import org.apache.commons.crypto.random.CryptoRandomFactory;
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
		
		read();
	}
	
	public boolean isEncrypted() {
		return tokens.isEncrypted;
	}
	
	public void addToken(String appname, String token, String password) {
		// wrong password
		if(!AuthEncrypt.getHash(password).equals(tokens.passwordhash))
			throw new RuntimeException("Wrong password");
		
		if(password != null) {
			token = AuthEncrypt.encrypt(password, Base64.decodeBase64(tokens.initvector), token);
		}
		
		addToken(appname, token);
	}
	
	public boolean checkPassword(String password) {
		if(password == null && !isEncrypted())
			return true;
		
		return AuthEncrypt.getHash(password).equals(tokens.passwordhash);
	}
	
	protected void addToken(String appname, String token) {
		
		tokens.tokens.add(new TokenEntry(appname, token));
	}
	
	public LinkedList<TokenEntry> getTokens() {
		return tokens.tokens;
	}
	
	protected void createNew(File authfile) throws IOException {
		tokens = new TokenDB();
		tokens.isEncrypted = false;
		tokens.tokens = new LinkedList<TokenEntry>();
		
		
	}
	
	public String getToken(int index, String password) {
		String s = getTokens().get(index).token;
		
		if(password == null)
			if(isEncrypted())
				return null;
			else
				return s;
		
		// wrong password
		if(!AuthEncrypt.getHash(password).equals(tokens.passwordhash))
			throw new RuntimeException("Wrong password");
		
		return AuthEncrypt.decrypt(password, Base64.decodeBase64(tokens.initvector), s);
		
	}
	
	public boolean encrypt(String oldpass, String password) throws GeneralSecurityException, IOException {
		if(!checkPassword(oldpass))
			return false;
		
		if(isEncrypted()) {
			// decrypt all the tokens
			for(int i = 0; i < getTokens().size(); i++) {
				tokens.tokens.set(i, new TokenEntry(tokens.tokens.get(i).appname, getToken(i, oldpass)));
			}
			
			tokens.isEncrypted = false;
			tokens.initvector = null;
			tokens.passwordhash = null;
		}
		
		if(password == null) {
			save();
			
			return true;
		}
		
		CryptoRandom cr = CryptoRandomFactory.getCryptoRandom();
		
		// CBC needs 128 bit IV
		byte[] iv = new byte[16];
		cr.nextBytes(iv);
		
		tokens.isEncrypted = true;
		tokens.initvector = Base64.encodeBase64String(iv);
		tokens.passwordhash = AuthEncrypt.getHash(password);
		
		LinkedList<TokenEntry> newtokens = new LinkedList<>();
		
		for(TokenEntry t : getTokens()) {
			String token = t.token;
			
			t.token = AuthEncrypt.encrypt(password, iv, token);
		}
		
		save();
		
		return true;
	}
	
	private void read() throws IOException {
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

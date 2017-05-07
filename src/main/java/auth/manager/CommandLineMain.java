package auth.manager;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;

import auth.GoogleAuth;
import auth.manager.TokenDB.TokenEntry;

public class CommandLineMain {

	public static void main(String[] args) throws Exception {
		if (args.length != 1) {
			System.err.println("Expected authfile as first argument");
			return;
		}
		File authfile = new File(args[0]);

		AuthManager am = new AuthManager(authfile);

		String password = null;

		if (am.isEncrypted()) {
			do {
				System.out.print("Password: ");
				password = new String(System.console().readPassword());
			} while (!am.checkPassword(password));
		}

		while (true) {
			System.out.print("> ");
			String line = System.console().readLine();
			String[] lineargs = line.split(" ");
			switch (lineargs[0]) {
			case "list":
				int i = 0;
				for (TokenEntry e : am.getTokens()) {
					i++;

					System.out.println("Token #" + i + ": " + e.appname);
				}

				if (am.getTokens().isEmpty())
					System.out.println("No tokens stored!");
				break;
			case "add":
				if (lineargs.length != 3)
					System.err.println("Expected 2 args: add <appname> <token>");
				am.addToken(lineargs[1], lineargs[2], password);
				am.save();
				break;
			case "remove":
				if (lineargs.length != 2)
					System.err.println("Expected 1 arg: remove <index>");
				try {
					am.getTokens().remove(Integer.parseInt(lineargs[1]));
				} catch (NumberFormatException e) {
					System.err.println("Expected a number for index! ");
				}
				am.save();
				break;
			case "getpin":
				if (lineargs.length != 2)
					System.err.println("Expected 1 arg: getpin <index>");
				int index;
				try {
					index = Integer.parseInt(lineargs[1]) - 1;
				} catch (NumberFormatException e) {
					System.err.println("Expected a number for index! ");
					return;
				}
				GoogleAuth ga = new GoogleAuth(am.getToken(index, password));
				String pin = ga.computePin();
				System.out.println("PIN: " + pin);
				
				StringSelection stringSelection = new StringSelection(pin);
				Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
				clpbrd.setContents(stringSelection, null);
				System.out.println("Copied to clipboard!");
				
				break;
			case "setpw":
				System.out.print("New Password: ");
				String npassword = new String(System.console().readPassword());

				System.out.print("Confirm new password: ");
				if (new String(System.console().readPassword()).equals(npassword)) {
					am.encrypt(password, npassword);
					System.out.println("Password updated!");
				} else {
					System.err.println("Passwords do not match; aborting.");
				}
				
				password = npassword;
				
				break;
			case "exit":
				return;
			case "help":
				System.out.println("list\t\tadd\t\tremove\t\tgetpin\t\tsetpw\t\texit\t\thelp");
			}
		}
	}

}

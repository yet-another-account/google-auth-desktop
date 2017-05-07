package auth;

import java.security.GeneralSecurityException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import auth.Base32String;

import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

// read ~/.google-authenticator
// display counter with dots (30 second refresh)
// jcuff@srv:~/auth/javaauth/jc$ java -classpath ./ Authenticator.Main /home/jcuff/.google_authenticator 

public class Main {
	Timer timer;

	public static void main(String[] args) {
		Main main = new Main();
		main.reminder(args[0]);
	}

	public void reminder(String secret) {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimedPin(secret), 0, 1 * 1000);
	}

	int count = 1;

	class TimedPin extends TimerTask {
		private String secret;

		public TimedPin(String secret) {
			this.secret = secret;
		}

		String previouscode = "";

		public void run() {
			GoogleAuth googleauth = new GoogleAuth(secret);
			String newout = googleauth.computePin();
			if (previouscode.equals(newout)) {
				System.out.print(".");
			} else {
				if (count <= 30) {
					for (int i = count + 1; i <= 30; i++) {
						System.out.print("+");
					}
				}
				System.out.println(":" + newout + ":");
				count = 0;
				//System.exit(0);
				
			}
			previouscode = newout;
			count++;
		}
	}


}

package queuebot;
import java.util.HashMap;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.Role;

public class QueueBot {

	public static HashMap<String, Queue> activeQueues = new HashMap<String,Queue>();

	public static void main(String[] args) {
		// Insert your bot's token here
		String token = "NjY4Njk0Mjk3OTM5NTQyMDE3.XiVBMw.SNyqNVVFJUINmWNctWnTw2nursM" + 
				"";

		DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();

		// Add a listener which answers with "Pong!" if someone writes "!ping"
		api.addMessageCreateListener(event -> {
			if (event.getMessageContent().startsWith("-q")) {
				String[] t = event.getMessageContent().split(" ");
				String[] a = new String[t.length-1];
				for (int i = 1; i < t.length; i++) {
					a[i-1] = t[i];
				}

				QueueCommandParser.parse(event, a);
			}
		});

		// Print the invite url of your bot
		System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
		System.out.println("Bot started!");
	}

}
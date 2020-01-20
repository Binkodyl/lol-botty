package queuebot;
import java.util.ArrayList;

import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

public class QueueCommandParser {

	public static void parse(MessageCreateEvent event, String[] args) {
		if ( args.length == 0 ) {
			event.getChannel().sendMessage("For help type: -q help");
			return;
		}
		
		String command = args[0];
		
		// User typing the message
		User user = event.getMessageAuthor().asUser().get();
		Role staffRole = event.getServer().get().getRolesByName("Staff").get(0);
		boolean isStaff = user.getRoles(event.getServer().get()).contains(staffRole);
		
		// Name always starts right after the command. If not typed, name will be blank.
		String originalName = "";
		for (int i = 1; i < args.length; i++) {
			originalName += args[i];
			if ( i < args.length-1 )
				originalName += " ";
		}
		String name = originalName.toLowerCase(); // This is the searchable name. Lowercased!
		
		
		// Help
		if ( command.equals("help") ) {
			event.getChannel().sendMessage("Commands:\n"
					+ "-q c (create) {name}\n"
					+ "-q l (leave) {name}\n"
					+ "-q ul (unlock) {name}\n"
					+ "-q jn (join) {name}\n"
					+ "-q lv (leave) {name}\n"
					+ "-q n (next) {name}\n"
					+ "-q e (end) {name}\n"
					+ "-q inf (info) {name}\n"
			        + "-q a (active) {name}");
		}
		
		// Create new queue
		if ( command.equals("c") ) {
			if ( !isStaff ) {
				event.getChannel().sendMessage("You don't have permission");
				return;
			}
			
			Queue queue = QueueBot.activeQueues.get(name);
			
			if ( queue == null ) {
				queue = new Queue(originalName);
				queue.setLocked(true);
				QueueBot.activeQueues.put(name, queue);
				
				event.getChannel().sendMessage("Created queue: " + queue.getName() + ". To unlock the queue type: -q ul (unlock) " + queue.getName());
			} else {
				event.getChannel().sendMessage("Queue " + queue.getName() + " already exists! Please use another name");
			}
		}
		
		// Unlock
		if ( command.equals("ul") ) {
			if ( !isStaff ) {
				event.getChannel().sendMessage("You don't have permission");
				return;
			}
			
			Queue queue = QueueBot.activeQueues.get(name);
			if ( queue == null ) {
				event.getChannel().sendMessage("Queue not found!");
				return;
			}
			
			queue.setLocked(false);
			event.getChannel().sendMessage("Queue " + queue.getName() + " has been unlocked!");
		}
		
		// Lock
		if ( command.equals("l") ) {
			if ( !isStaff ) {
				event.getChannel().sendMessage("You don't have permission");
				return;
			}
			
			Queue queue = QueueBot.activeQueues.get(name);
			if ( queue == null ) {
				event.getChannel().sendMessage("Queue not found!");
				return;
			}
			
			queue.setLocked(true);
			event.getChannel().sendMessage("Queue " + queue.getName() + " has been locked!");
		}
		
		// Join
		if ( command.equals("jn") ) {
			Queue queue = QueueBot.activeQueues.get(name);
			if ( queue == null ) {
				event.getChannel().sendMessage("Queue not found!");
				return;
			}
			
			if ( queue.isLocked() ) {
				event.getChannel().sendMessage("This queue is locked!");
				return;
			}
			
			if ( queue.getUser( user ) == null ) {
				queue.join(user);
				event.getChannel().sendMessage("You've joined queue "+queue.getName()+"! Position: " + queue.getPosition(user));
			} else {
				event.getChannel().sendMessage("You are already in the queue! Position: " + queue.getPosition(user));
			}
		}
		
		// Leave
		if ( command.equals("lv") ) {
			Queue queue = QueueBot.activeQueues.get(name);
			if ( queue == null ) {
				event.getChannel().sendMessage("Queue not found!");
				return;
			}
			
			if ( queue.getUser(user) == null ) {
				event.getChannel().sendMessage("You are already not in this queue");
				return;
			} else {
				queue.leave(user);
				event.getChannel().sendMessage("You have left queue " + queue.getName());
			}
		}
		
		// Next
		if ( command.equals("n") ) {
			if ( !isStaff ) {
				event.getChannel().sendMessage("You don't have permission");
				return;
			}
			
			Queue queue = QueueBot.activeQueues.get(name);
			if ( queue == null ) {
				event.getChannel().sendMessage("Queue not found!");
				return;
			}
			
			// Get next in line
			User next = queue.next();
			if ( next == null ) {
				event.getChannel().sendMessage("Queue is empty!");
				return;
			} else {
				
				// Tell the on deck user to get ready
				String suffix = "";
				if ( queue.size() > 0 ) {
					User onDeck = queue.getUser(0);
					suffix += onDeck.getMentionTag() + "You're next in line!";
				}
				
				event.getChannel().sendMessage("[Queue " + queue.getName() + "]:\n\tHey " + next.getMentionTag() + " ! Come on down! " + suffix);
			}
		}
		
		// End
		if ( command.equals("e") ) {
			Queue queue = QueueBot.activeQueues.get(name);
			if ( queue == null ) {
				event.getChannel().sendMessage("Queue not found!");
				return;
			}
			
			// Delete the queue
			event.getChannel().sendMessage("Queue " + queue.getName() + " has finished");
			queue.clear();
			QueueBot.activeQueues.remove(name);
		}
		
		// Info
		if ( command.equals("inf") ) {
			Queue queue = QueueBot.activeQueues.get(name);
			if ( queue == null ) {
				// Get list of queues
				ArrayList<Queue> queues = new ArrayList<Queue>();
				String[] keys = QueueBot.activeQueues.keySet().toArray(new String[QueueBot.activeQueues.size()]);
				for (int i = 0; i < keys.length; i++) {
					queues.add(QueueBot.activeQueues.get(keys[i]));
				}

				// Initial text
				String str = "";
				if ( queues.size() == 0 ) {
					str += "There are no active queues\n";
				} else {
					str += "There are " + queues.size() + " active queues\n";
				}
				
				// List queues
				for (int i = 0; i < queues.size(); i++) {
					queue = queues.get(i);
					String suffix = "";
					if ( queue.isLocked() ) {
						suffix += " (locked)";
					}
					
					str += "\t\t\t" + queue.getName() + "(" + queue.size() + ")" + suffix + "\n";
				}
				
				// Send
				event.getChannel().sendMessage(str);
			} else {
				
				// Give info about specific queue
				String str = "";
				str = "[Queue " + queue.getName() + "]:\n"
								+ "\t\t\t\t\tLength: " + queue.size() + "\n"
								+ "\t\t\t\t\tPosition: " + queue.getPosition(user)+"\n";
				
				str += "\t\t---------------------\n";
				User[] users = queue.getUsers();
				for (int i = 0; i < users.length; i++) {
					str += "\t\t" + i + ") " + users[i].getName() + "\n";
				}
				
				event.getChannel().sendMessage(str);
			}
		}
		
		// Active
		if ( command.equals("active") ) {
			ArrayList<Queue> queues = new ArrayList<Queue>();
			String[] keys = QueueBot.activeQueues.keySet().toArray(new String[QueueBot.activeQueues.size()]);
			for (int i = 0; i < keys.length; i++) {
				queues.add(QueueBot.activeQueues.get(keys[i]));
			}

			String str = "";
			if ( queues.size() == 0 ) {
				str += "There are no active queues\n";
			} else {
				str += "There are " + queues.size() + " active queues\n";
			}
			
			for (int i = 0; i < queues.size(); i++) {
				Queue queue = queues.get(i);
				String suffix = "";
				if ( queue.isLocked() ) {
					suffix += " (locked)";
				}
				
				str += "\t\t\t" + queue.getName() + "(" + queue.size() + ")" + suffix + "\n";
			}
			
			event.getChannel().sendMessage(str);
		}
	}

}

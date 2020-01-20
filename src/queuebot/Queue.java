package queuebot;
import java.util.ArrayList;

import org.javacord.api.entity.user.User;

public class Queue {
	private String name;
	private boolean locked;
	
	private ArrayList<User> users = new ArrayList<User>();
	
	public Queue(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the locked flag for this queue.
	 * @param locked
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	/**
	 * Returns if queue is currently locked. A locked queue SHOULD not be able to be joined.
	 * @return
	 */
	public boolean isLocked() {
		return this.locked;
	}
	
	/**
	 * Adds a user to a queue. Does not check if user is automatically in queue. Check yourself.
	 * @param user
	 */
	public void join( User user ) {
		users.add(user);
	}
	
	/**
	 * Leaves a user from the queue. Does not check if they are already in it.
	 * @param user
	 */
	public void leave(User user) {
		int index = getPosition(user);
		if ( index == -1 )
			return;
		
		users.remove(index);
	}
	
	/**
	 * Return an unmodifyable list of users in the queue
	 * @return
	 */
	public User[] getUsers() {
		return users.toArray(new User[users.size()]);
	}

	/**
	 * Return the user in this queue. If the user is not in the queue, return null.
	 * @param user
	 * @return
	 */
	public User getUser(User user) {
		int index = getPosition(user);
		if ( index == -1 )
			return null;
		
		return users.get(index);
	}
	
	/**
	 * Return the user at a specific position of this queue. Does not do bounds checks.
	 * @param i
	 * @return
	 */
	public User getUser(int i) {
		return users.get(i);
	}
	
	/**
	 * Return the index position of the user in this queue. Returns -1 if user is not in queue.
	 * @param user
	 * @return
	 */
	public int getPosition( User user ) {
		for (int i = 0; i < users.size(); i++) {
			if ( users.get(i).equals(user) )
				return i;
		}
		
		return -1;
	}

	/**
	 * Returns the original name of the queue how it's author typed it.
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Nexts the queue. The user that is returned is removed from the queue.
	 * @return
	 */
	public User next() {
		if ( users.size() > 0 ) {
			User t = users.remove(0);
			return t;
		}
		
		return null;
	}

	/**
	 * Clears the queue
	 */
	public void clear() {
		users.clear();
	}

	/**
	 * Returns the size of the queue.
	 * @return
	 */
	public int size() {
		return users.size();
	}
}

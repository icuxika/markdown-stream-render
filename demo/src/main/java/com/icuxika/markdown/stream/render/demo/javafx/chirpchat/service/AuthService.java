package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.service;

import com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model.User;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AuthService {
	private final Map<String, User> users = new HashMap<>();
	private User currentUser;

	public AuthService() {
		initializeSampleUsers();
	}

	private void initializeSampleUsers() {
		User user1 = new User(1, "johndoe", "John Doe", "john@example.com");
		user1.setBio("Software developer | Coffee lover");
		user1.setLocation("San Francisco, CA");
		user1.setFollowersCount(1234);
		user1.setFollowingCount(567);
		user1.setVerified(true);
		users.put("johndoe", user1);

		User user2 = new User(2, "janesmith", "Jane Smith", "jane@example.com");
		user2.setBio("Designer & Creator");
		user2.setLocation("New York, NY");
		user2.setFollowersCount(2345);
		user2.setFollowingCount(890);
		users.put("janesmith", user2);

		User user3 = new User(3, "devmaster", "Dev Master", "dev@example.com");
		user3.setBio("Full-stack developer | Open source contributor");
		user3.setLocation("Seattle, WA");
		user3.setFollowersCount(5678);
		user3.setFollowingCount(234);
		user3.setVerified(true);
		users.put("devmaster", user3);
	}

	public Optional<User> login(String username, String password) {
		User user = users.get(username);
		if (user != null) {
			currentUser = user;
			return Optional.of(user);
		}
		return Optional.empty();
	}

	public Optional<User> register(String username, String email, String displayName, String password) {
		if (users.containsKey(username)) {
			return Optional.empty();
		}

		long newId = users.size() + 1;
		User newUser = new User(newId, username, displayName, email);
		newUser.setFollowersCount(0);
		newUser.setFollowingCount(0);
		users.put(username, newUser);
		currentUser = newUser;
		return Optional.of(newUser);
	}

	public void logout() {
		currentUser = null;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public boolean isLoggedIn() {
		return currentUser != null;
	}

	public Map<String, User> getUsers() {
		return users;
	}
}

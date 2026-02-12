package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Conversation {
	private long id;
	private User otherUser;
	private List<Message> messages;
	private LocalDateTime lastMessageTime;
	private int unreadCount;

	public Conversation() {
		this.messages = new ArrayList<>();
	}

	public Conversation(long id, User otherUser) {
		this.id = id;
		this.otherUser = otherUser;
		this.messages = new ArrayList<>();
		this.unreadCount = 0;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getOtherUser() {
		return otherUser;
	}

	public void setOtherUser(User otherUser) {
		this.otherUser = otherUser;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public LocalDateTime getLastMessageTime() {
		if (messages.isEmpty()) {
			return null;
		}
		return messages.get(messages.size() - 1).getCreatedAt();
	}

	public void setLastMessageTime(LocalDateTime lastMessageTime) {
		this.lastMessageTime = lastMessageTime;
	}

	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public String getLastMessagePreview() {
		if (messages.isEmpty()) {
			return "No messages yet";
		}
		Message last = messages.get(messages.size() - 1);
		String content = last.getContent();
		if (content.length() > 50) {
			return content.substring(0, 50) + "...";
		}
		return content;
	}

	public String getFormattedTime() {
		LocalDateTime time = getLastMessageTime();
		if (time == null) {
			return "";
		}
		long minutes = java.time.Duration.between(time, LocalDateTime.now()).toMinutes();
		if (minutes < 1) {
			return "now";
		} else if (minutes < 60) {
			return minutes + "m";
		} else if (minutes < 1440) {
			return (minutes / 60) + "h";
		} else {
			return time.toLocalDate().toString();
		}
	}

	public void addMessage(Message message) {
		messages.add(message);
	}
}

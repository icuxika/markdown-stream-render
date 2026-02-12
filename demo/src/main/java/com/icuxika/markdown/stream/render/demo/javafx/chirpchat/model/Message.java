package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model;

import java.time.LocalDateTime;

public class Message {
	private long id;
	private User sender;
	private User recipient;
	private String content;
	private String imageUrl;
	private LocalDateTime createdAt;
	private boolean read;
	private boolean sent;

	public Message() {
	}

	public Message(long id, User sender, User recipient, String content) {
		this.id = id;
		this.sender = sender;
		this.recipient = recipient;
		this.content = content;
		this.createdAt = LocalDateTime.now();
		this.read = false;
		this.sent = true;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getRecipient() {
		return recipient;
	}

	public void setRecipient(User recipient) {
		this.recipient = recipient;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isSent() {
		return sent;
	}

	public void setSent(boolean sent) {
		this.sent = sent;
	}

	public String getFormattedTime() {
		if (createdAt == null) {
			return "";
		}
		return createdAt.toLocalTime().toString().substring(0, 5);
	}
}

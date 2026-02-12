package com.icuxika.markdown.stream.render.demo.javafx.chirpchat.model;

import java.time.LocalDateTime;

public class Post {
	private long id;
	private User author;
	private String content;
	private String imageUrl;
	private int likeCount;
	private int retweetCount;
	private int replyCount;
	private int viewCount;
	private LocalDateTime createdAt;
	private boolean liked;
	private boolean retweeted;
	private boolean pinned;

	public Post() {
	}

	public Post(long id, User author, String content) {
		this.id = id;
		this.author = author;
		this.content = content;
		this.createdAt = LocalDateTime.now();
		this.likeCount = 0;
		this.retweetCount = 0;
		this.replyCount = 0;
		this.viewCount = 0;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
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

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getRetweetCount() {
		return retweetCount;
	}

	public void setRetweetCount(int retweetCount) {
		this.retweetCount = retweetCount;
	}

	public int getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(int replyCount) {
		this.replyCount = replyCount;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isLiked() {
		return liked;
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	public boolean isRetweeted() {
		return retweeted;
	}

	public void setRetweeted(boolean retweeted) {
		this.retweeted = retweeted;
	}

	public boolean isPinned() {
		return pinned;
	}

	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	public String getFormattedTime() {
		if (createdAt == null) {
			return "";
		}
		long minutes = java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
		if (minutes < 1) {
			return "now";
		} else if (minutes < 60) {
			return minutes + "m";
		} else if (minutes < 1440) {
			return (minutes / 60) + "h";
		} else {
			return createdAt.toLocalDate().toString();
		}
	}
}

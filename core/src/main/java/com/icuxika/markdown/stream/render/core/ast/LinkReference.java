package com.icuxika.markdown.stream.render.core.ast;

public class LinkReference {
	private final String label;
	private final String destination;
	private final String title;

	/**
	 * Constructor.
	 *
	 * @param label
	 *            label
	 * @param destination
	 *            destination
	 * @param title
	 *            title
	 */
	public LinkReference(String label, String destination, String title) {
		this.label = label;
		this.destination = destination;
		this.title = title;
	}

	public String getLabel() {
		return label;
	}

	public String getDestination() {
		return destination;
	}

	public String getTitle() {
		return title;
	}
}

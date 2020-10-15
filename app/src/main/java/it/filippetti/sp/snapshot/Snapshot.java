package it.filippetti.sp.snapshot;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Snapshot extends Event {
	private List<IChannel> channelCollection = new ArrayList<>();
	/* */
	// private IAddress address;
	private URI reference;
	private URI source;
	private URI destination;

	private String referenceType;

	/* snapshot category */
	private String category;

	/* snapshot context */
	private String context;

	/* */
	private Long sequenceNumber;
	private Long responseNumber;

	/**/

	private Map<String, Object> meta = new LinkedHashMap<>();

	public Snapshot() {
	}

	public Snapshot(Calendar timestamp) {
		this();
		this.setTimestamp(timestamp);
	}

	public Snapshot(Long timestamp) {
		this();
		this.setTimestamp(timestamp);
	}

	public void clear() {
		channelCollection.clear();
	}

	public String getCategory() {
		return category;
	}

	public String getContext() {
		return context;
	}

	public URI getDestination() {
		return destination;
	}

	public List<IChannel> getChannelCollection() {
		return channelCollection;
	}

//	public Stream<IChannel> getChannelCollection(String key) {
//		return channelCollection.stream().filter(h -> key.equals(h.getKey()));
//	}

	public Map<String, Object> getMeta() {
		return meta;
	}

	public URI getReference() {
		return reference;
	}

	public String getReferenceType() {
		return referenceType;
	}

	public Long getResponseNumber() {
		return responseNumber;
	}

	public Long getSequenceNumber() {
		return sequenceNumber;
	}

	public URI getSource() {
		return source;
	}

	@Deprecated
	public String getType() {
		return referenceType;
	}

	public void setCategory(String snapshotType) {
		this.category = snapshotType;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public void setDestination(URI destination) {
		this.destination = destination;
	}

	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	public void setReference(URI reference) {
		this.reference = reference;
	}

	public void setReferenceType(String referenceType) {
		this.referenceType = referenceType;
	}

	public void setResponseNumber(Long responseNumber) {
		this.responseNumber = responseNumber;
	}

	public void setSequenceNumber(Long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public void setSource(URI source) {
		this.source = source;
	}

	@Deprecated
	public void setType(String type) {
		this.referenceType = type;
	}

	public void putChannel(IChannel m) {
		Iterator<IChannel> it = channelCollection.iterator();
		while (it.hasNext()) {
			IChannel f = it.next();

			boolean checkSource = f.getSource() != null && m.getSource() != null ? f.getSource().equals(m.getSource()) : (f.getSource() == null && m.getSource() == null);
			boolean checkTarget = f.getTarget() != null && m.getTarget() != null ? f.getTarget().equals(m.getTarget()) : (f.getTarget() == null && m.getTarget() == null);

			if (f.getKey().equals(m.getKey()) && checkSource && checkTarget) {
				it.remove();
			}
		}
		
		channelCollection.add(m);
	}

	public void removeChannel(String key) {
		Iterator<IChannel> it = channelCollection.iterator();
		while (it.hasNext()) {
			IChannel f = it.next();

			if (f.getKey().equals(key)) {
				it.remove();
			}
		}
	}

	public void removeChannel(String key, URI source) {
		Iterator<IChannel> it = channelCollection.iterator();
		while (it.hasNext()) {
			IChannel f = it.next();

			boolean checkSource = f.getSource() != null && source != null ? f.getSource().equals(source) : (f.getSource() == null && source == null);
			if (f.getKey().equals(key) && checkSource) {
				it.remove();
			}
		}
	}

	public void removeChannel(String key, URI source, URI target) {
		Iterator<IChannel> it = channelCollection.iterator();
		while (it.hasNext()) {
			IChannel f = it.next();

			boolean checkSource = f.getSource() != null && source != null ? f.getSource().equals(source) : (f.getSource() == null && source == null);
			boolean checkTarget = f.getTarget() != null && target != null ? f.getTarget().equals(target) : (f.getTarget() == null && target == null);
			if (f.getKey().equals(key) && checkSource && checkTarget) {
				it.remove();
			}
		}
	}

}

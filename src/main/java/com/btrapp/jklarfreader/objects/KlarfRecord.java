package com.btrapp.jklarfreader.objects;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public final class KlarfRecord {
	public KlarfRecord(String name, String id) {
		this.name = name;
		this.id = id;
	}

	private String name; //ex: FileRecord, LotRecord, WaferRecord...
	private String id = ""; //1.8  (Some Records don't have an ID.  Use a blank string for those)
	private LinkedHashMap<String, List<String>> fields = new LinkedHashMap<>();
	private List<KlarfList> lists = new ArrayList<>();
	private List<KlarfRecord> records = new ArrayList<>();

	public Optional<List<String>> findField(String name) {
		return fields.entrySet().stream().filter(e->name.equalsIgnoreCase(e.getKey()))
			.map(Entry::getValue)
			.findFirst();
	}
	/**
	 * A case-insensitive search to match a record by name.
	 * @param name
	 * @return
	 */
	public List<KlarfRecord> findRecordsByName(String name) {
		return records.stream().filter(r->name.equalsIgnoreCase(r.getName()))
			.collect(Collectors.toList());
	}
	
	/**
	 * A case-insensitive search to find a list by name.  I suppose it could be listed
	 *  twice, so return a stream.
	 * @param string
	 * @return
	 */
	public Optional<KlarfList> findListByName(String name) {
		return lists.stream().filter(r->name.equalsIgnoreCase(r.getName()))
				.findFirst();
	}
	
	/**
	 * A case-insensitive search to match a record by name and an ID
	 * @param name
	 * @return
	 */
	public Optional<KlarfRecord> findRecordByNameAndId(String name, String id) {
		return records.stream()
				.filter(r->name.equalsIgnoreCase(r.getName()))
				.filter(r->id.equalsIgnoreCase(r.getId()))
				.findFirst();
	}
	
	
	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	public LinkedHashMap<String, List<String>> getFields() {
		return fields;
	}

	public List<KlarfList> getLists() {
		return lists;
	}

	public List<KlarfRecord> getRecords() {
		return records;
	}

}
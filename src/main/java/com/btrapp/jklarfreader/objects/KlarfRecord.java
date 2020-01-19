package com.btrapp.jklarfreader.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** @author btrapp */

/*
 * Example:
 *
 * Record WaferRecord "FirstWaferId" {
 * Field DieOrigin 2 {0, 0}
 * Field OrientationInstructions 1 {""}
 * }
 *
 * Would map to:
 *
 * name: "WaferRecord"
 * id: "FirstWaferId"
 * fields: {
 * "DieOrigin" : ["0","0"],
 * "OrientationInstructions" : [""]
 * }
 *
 */
public final class KlarfRecord {
	// Keep a zero-arg contstructor around so this can object can be created by the Jackson
	// objectmapper
	@SuppressWarnings("unused")
	private KlarfRecord() {
	}

	public KlarfRecord(String name, String id) {
		this.name = name;
		this.id = id;
	}

	private String name; // ex: FileRecord, LotRecord, WaferRecord...
	private String id = ""; // 1.8  (Some Records don't have an ID.  Use a blank string for those)
	private LinkedHashMap<String, List<String>> fields = new LinkedHashMap<>();
	private List<KlarfList> lists = new ArrayList<>();
	private List<KlarfRecord> records = new ArrayList<>();

	/**
	 * A <b>case-insensitive</b> search to match a record by name.
	 *
	 * @param name
	 *            the name of the record do find (WaferRecord)
	 * @return all matching record objects
	 */
	public List<KlarfRecord> findRecordsByName(String name) {
		return records.stream()
				.filter(r -> name.equalsIgnoreCase(r.getName()))
				.collect(Collectors.toList());
	}

	/**
	 * A <b>case-insensitive</b> search to find a list by name. Multiple records could match, so
	 * return a stream.
	 *
	 * @param name
	 *            the name of the list to find (DefectList)
	 * @return all matching list objects
	 */
	public List<KlarfList> findListsByName(String name) {
		return lists.stream()
				.filter(r -> name.equalsIgnoreCase(r.getName()))
				.collect(Collectors.toList());
	}

	/**
	 * A <b>case-insensitive</b> search to match a record by name and an ID
	 *
	 * @param name
	 *            the name of the record to find (WaferRecord)
	 * @param id
	 *            the ID of the record to find (MyWafer.01)
	 * @return the record
	 */
	public Optional<KlarfRecord> findRecordByNameAndId(String name, String id) {
		return records.stream()
				.filter(r -> name.equalsIgnoreCase(r.getName()))
				.filter(r -> id.equalsIgnoreCase(r.getId()))
				.findFirst();
	}

	/**
	 * A <b>case-insensitive</b> search to find a field
	 * 
	 * If the field is not present an empty list is returned.
	 *
	 * @param name
	 *            the name of the field to find
	 * @return the list of values
	 */
	public List<String> findField(String name) {
		return fields.entrySet().stream()
				.filter(e -> name.equalsIgnoreCase(e.getKey()))
				.map(e -> e.getValue())
				.findFirst()
				.orElse(Collections.emptyList());
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

	/**
	 * requireList:
	 * Find a list by name, ensures it has the requested length
	 * 
	 * @param name
	 *            the name of the list (case insensitve)
	 * @param n
	 *            the exact length required.
	 * @return the matching lists
	 * @throws KlarfContentException
	 */
	public List<KlarfList> reqList(String name, int n) throws KlarfContentException {
		List<KlarfList> lists = findListsByName(name);
		if (lists.isEmpty()) {
			throw new KlarfContentException("List " + name + " in record " + this.getName() + "/" + this.getId() + " is missing");
		}
		if (lists.size() != n) {
			throw new KlarfContentException(
					"List " + name + " in record " + this.getName() + "/" + this.getId() + " has the wrong length (" + lists.size() + " vs expected " + n + ")");
		}
		return lists;
	}

	/**
	 * requireRecord:
	 * Find a record by name, ensures it has the requested length
	 * 
	 * @param name
	 *            the name of the record (case insensitve)
	 * @param n
	 *            the exact length required.
	 * @return the matching records
	 * @throws KlarfContentException
	 */
	public List<KlarfRecord> reqRecord(String name, int n) throws KlarfContentException {
		List<KlarfRecord> records = findRecordsByName(name);
		if (records.isEmpty()) {
			throw new KlarfContentException("Record " + name + " in record " + this.getName() + "/" + this.getId() + " is missing");
		}
		if (records.size() != n) {
			throw new KlarfContentException(
					"Record " + name + " in record " + this.getName() + "/" + this.getId() + " has the wrong length (" + records.size() + " vs expected " + n + ")");
		}
		return records;

	}

	/**
	 * requireField:
	 * Finds a field by name, ensures it has the requested length
	 * 
	 * @param name
	 *            the field (case insensitive)
	 * @param n
	 *            the required number of items
	 * @return the field contents (as a List<String>)
	 * @throws KlarfContentException
	 */
	public List<String> reqField(String name, int n) throws KlarfContentException {
		List<String> fieldVals = this.findField(name);
		if (fieldVals.isEmpty()) {
			throw new KlarfContentException("Field " + name + " in record " + this.getName() + "/" + this.getId() + " is missing");
		}
		if (fieldVals.size() != n) {
			throw new KlarfContentException(
					"Field " + name + " in record " + this.getName() + "/" + this.getId() + " has the wrong length (" + fieldVals.size() + " vs expected " + n + ")");
		}
		return fieldVals;
	}

	/**
	 * All the logic of reqField but adds in the requirement that all values parse to Double
	 * 
	 * @param name
	 *            the field (case insensitive)
	 * @param n
	 *            the required number of items
	 * @return list of Doubles
	 * @throws KlarfContentException
	 */
	public List<Double> reqDoubleField(String name, int n) throws KlarfContentException {
		List<String> str = reqField(name, n);
		if (str.isEmpty()) {
			return Collections.emptyList();
		}
		try {
			return str.stream().map(Double::parseDouble).collect(Collectors.toList());
		} catch (Exception ex) {
			throw new KlarfContentException("Record " + this.getName() + "/" + this.getId() + " field " + name + " could not be parsed to double: " + str.toString());
		}
	}

	/**
	 * All the logic of reqField but adds in the requirement that all values parse to Integer
	 * 
	 * @param name
	 *            the field (case insensitive)
	 * @param n
	 *            the required number of items
	 * @return list of Integer
	 * @throws KlarfContentException
	 */
	public List<Integer> reqIntField(String name, int n) throws KlarfContentException {
		List<String> str = reqField(name, n);
		if (str.isEmpty()) {
			return Collections.emptyList();
		}
		try {
			return str.stream().map(Integer::parseInt).collect(Collectors.toList());
		} catch (Exception ex) {
			throw new KlarfContentException("Record " + this.getName() + "/" + this.getId() + " field " + name + " could not be parsed to double: " + str.toString());
		}
	}
}

package com.btrapp.jklarfreader.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author btrapp
 */

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
	private Set<String> quotedFields = new HashSet<>(); // Which fields shoudl be surrounded by quotes?
	private List<KlarfList> lists = new ArrayList<>();
	private List<KlarfRecord> records = new ArrayList<>();

	/**
	 * Adds a record to the record
	 *
	 * @param record
	 *            the record to add
	 */
	public void addRecord(KlarfRecord record) {
		this.records.add(record);
	}

	/**
	 * Adds a list to the record
	 *
	 * @param list
	 *            the list to add
	 */
	public void addList(KlarfList list) {
		this.lists.add(list);
	}

	/**
	 * Given a list, replace all lists with the same name,
	 * or add the list if there's not a list with the same name.
	 *
	 * @param list
	 *            the list to add
	 */
	public void setListByName(KlarfList newList) {
		List<KlarfList> newLists = new ArrayList<>(this.lists.size());
		boolean found = false;
		for (KlarfList oldList : this.lists) {
			if (oldList.getName().equals(newList.getName())) {
				//use new list instead
				newLists.add(newList);
				found = true;
			} else {
				//Keep
				newLists.add(oldList);
			}
		}
		if (!found) {
			newLists.add(newList);
		}
		this.lists = newLists;
	}

	/**
	 * Sets a field in the record (dupe field names will overwrite), and sets a flag saying it should
	 * be quoted if printed later.
	 *
	 * @param fieldName
	 *            the name of the field (case sensitive)
	 * @param fieldValue
	 *            the value to set
	 */
	public void setStringField(String fieldName, List<String> fieldValue) {
		setField(fieldName, fieldValue, true);
	}

	/**
	 * Sets a field in the record (dupe field names will overwrite), and sets a flag saying it should
	 * NOT be quoted if printed later.
	 *
	 * @param fieldName
	 *            the name of the field (case sensitive)
	 * @param fieldValue
	 *            the value to set
	 */
	public void setNumericField(String fieldName, List<Number> fieldValue) {
		setField(
				fieldName, fieldValue.stream().map(Number::toString).collect(Collectors.toList()), false);
	}

	/**
	 * Sets a field in the record (dupe field names will overwrite)
	 *
	 * @param fieldName
	 * @param fieldValue
	 * @param isQuoted
	 *            - true if this field was originally (or should) be quoted "like this" "example"
	 */
	public void setField(String fieldName, List<String> fieldValue, boolean isQuoted) {
		this.fields.put(fieldName, fieldValue);
		if (isQuoted)
			quotedFields.add(fieldName);
		else
			quotedFields.remove(fieldName);
	}

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
	 * <p>
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

	/**
	 * A <b>case-insensitive</b> search to see if a field is quoted
	 *
	 * @param fieldName
	 *            the name of the field (case sensitive!)
	 * @return the quote state
	 */
	public boolean isQuotedField(String fieldName) {
		return this.quotedFields.contains(fieldName);
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return id;
	}

	/**
	 * Returns an Immutable LinkedHashMap
	 *
	 * @return the field map (name to value list)
	 */
	public Map<String, List<String>> getFields() {
		return Collections.unmodifiableMap(fields);
	}

	/**
	 * @return Immutable list of lists
	 */
	public List<KlarfList> getLists() {
		return Collections.unmodifiableList(lists);
	}

	/**
	 * @return Immutable list of records
	 */
	public List<KlarfRecord> getRecords() {
		return Collections.unmodifiableList(records);
	}

	/**
	 * reqList: Find a list by name, ensures it has the requested length
	 *
	 * @param name
	 *            the name of the list (case insensitve)
	 * @param n
	 *            the exact length required.
	 * @return the matching lists
	 * @throws KlarfContentException
	 *             if the list is missing or the length is unexpected
	 */
	public List<KlarfList> reqList(String name, int n) throws KlarfContentException {
		List<KlarfList> lists = findListsByName(name);
		if (lists.isEmpty()) {
			throw new KlarfContentException(
					"List " + name + " in record " + this.getName() + "/" + this.getId() + " is missing");
		}
		if (lists.size() != n) {
			throw new KlarfContentException(
					"List "
							+ name
							+ " in record "
							+ this.getName()
							+ "/"
							+ this.getId()
							+ " has the wrong length ("
							+ lists.size()
							+ " vs expected "
							+ n
							+ ")");
		}
		return lists;
	}

	/**
	 * reqRecord: Find a record by name, ensures it has the requested length
	 *
	 * @param name
	 *            the name of the record (case insensitve)
	 * @param n
	 *            the exact length required.
	 * @return the matching records
	 * @throws KlarfContentException
	 *             if the record is missing or the length is unexpected
	 */
	public List<KlarfRecord> reqRecord(String name, int n) throws KlarfContentException {
		List<KlarfRecord> records = findRecordsByName(name);
		if (records.isEmpty()) {
			throw new KlarfContentException(
					"Record " + name + " in record " + this.getName() + "/" + this.getId() + " is missing");
		}
		if (records.size() != n) {
			throw new KlarfContentException(
					"Record "
							+ name
							+ " in record "
							+ this.getName()
							+ "/"
							+ this.getId()
							+ " has the wrong length ("
							+ records.size()
							+ " vs expected "
							+ n
							+ ")");
		}
		return records;
	}

	/**
	 * reqField: Finds a field by name, ensures it has the requested length
	 *
	 * @param name
	 *            the field (case insensitive)
	 * @param n
	 *            the required number of items
	 * @return the field contents (as a List &lt;String&gt; )
	 * @throws KlarfContentException
	 *             if the field is missing or the length is unexpected
	 */
	public List<String> reqField(String name, int n) throws KlarfContentException {
		List<String> fieldVals = this.findField(name);
		if (fieldVals.isEmpty()) {
			throw new KlarfContentException(
					"Field " + name + " in record " + this.getName() + "/" + this.getId() + " is missing");
		}
		if (fieldVals.size() != n) {
			throw new KlarfContentException(
					"Field "
							+ name
							+ " in record "
							+ this.getName()
							+ "/"
							+ this.getId()
							+ " has the wrong length ("
							+ fieldVals.size()
							+ " vs expected "
							+ n
							+ ")");
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
	 *             if the parse to double fails
	 */
	public List<Double> reqDoubleField(String name, int n) throws KlarfContentException {
		List<String> str = reqField(name, n);
		try {
			return str.stream().map(Double::parseDouble).collect(Collectors.toList());
		} catch (Exception ex) {
			throw new KlarfContentException(
					"Record "
							+ this.getName()
							+ "/"
							+ this.getId()
							+ " field "
							+ name
							+ " could not be parsed to double: "
							+ str.toString());
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
	 *             KlarfContentException if the parse to integer fails
	 */
	public List<Integer> reqIntField(String name, int n) throws KlarfContentException {
		List<String> str = reqField(name, n);
		try {
			return str.stream().map(Integer::parseInt).collect(Collectors.toList());
		} catch (Exception ex) {
			throw new KlarfContentException(
					"Record "
							+ this.getName()
							+ "/"
							+ this.getId()
							+ " field "
							+ name
							+ " could not be parsed to int: "
							+ str.toString());
		}
	}
}

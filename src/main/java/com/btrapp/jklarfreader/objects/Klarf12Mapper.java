package com.btrapp.jklarfreader.objects;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Klarf12Mapper {
  private static Klarf12Mapper instance;

  public enum KlarfDataType {
    Record,
    Ignored_Record,
    Field,
    List,
    Ignored_Field,
    Unsupported,
    EndOfFile
  }

  public enum KlarfDataLevel {
    File,
    Lot,
    Wafer,
    Test,
    Unsupported
  }

  public record KlarfMappingRecord(
      String klarfKey12,
      KlarfDataType klarfDataType,
      KlarfDataLevel klarfDataLevel,
      String klarfKey18) {}

  public static KlarfMappingRecord unsupportedKlarfMappingRecord =
      new KlarfMappingRecord(
          "UNSUPPORTED", KlarfDataType.Unsupported, KlarfDataLevel.Unsupported, "Unsupported");

  private List<KlarfMappingRecord> mappingRecords = new ArrayList<>();
  private List<String[]> listColTypes = new ArrayList<>();
  private Map<String, String> unitConversion = new HashMap<>();

  private Klarf12Mapper() {}

  public static Klarf12Mapper getInstance() throws Exception {
    if (instance == null) {
      instance = new Klarf12Mapper();
      try (final BufferedReader br =
          new BufferedReader(
              new InputStreamReader(
                  Klarf12Mapper.class.getResourceAsStream("klarf12_18_mapping.txt")))) {
        // instance.props.load(stream);
        while (br.ready()) {
          String line = br.readLine();
          if (line.isBlank()) continue;
          if (!line.startsWith("#")) {
            String[] parts = line.split(",");
            if (parts.length == 4) {
              instance.mappingRecords.add(
                  new KlarfMappingRecord(
                      parts[0].trim().toUpperCase(),
                      KlarfDataType.valueOf(parts[1].trim()),
                      KlarfDataLevel.valueOf(parts[2].trim()),
                      parts[3].trim()));
            } else {
              throw new Exception("Error initilizing mapping on line " + line);
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      try (final BufferedReader br =
          new BufferedReader(
              new InputStreamReader(
                  Klarf12Mapper.class.getResourceAsStream("klarf12_18_list_types.txt")))) {
        while (br.ready()) {
          String line = br.readLine();
          if (line.isBlank()) continue;
          if (!line.startsWith("#")) {
            String[] parts = line.split(",");
            if (parts.length != 4) {
              throw new Exception("Error initilizing list column types " + line);
            }
            instance.listColTypes.add(
                new String[] {
                  parts[0].trim().toUpperCase(), parts[1].trim(), parts[2].trim(), parts[3].trim()
                });
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      try (final BufferedReader br =
          new BufferedReader(
              new InputStreamReader(
                  Klarf12Mapper.class.getResourceAsStream("klarf12_18_unit_conversion.txt")))) {
        while (br.ready()) {
          String line = br.readLine();
          if (line.isBlank()) continue;
          if (!line.startsWith("#")) {
            String[] parts = line.split(",");
            if (parts.length != 2) {
              throw new Exception("Error initilizing unit conversions " + line);
            }
            instance.unitConversion.put(
                parts[0].trim().toUpperCase(), parts[1].trim().toUpperCase());
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return instance;
  }

  public static void main(String[] args) throws Exception {
    Klarf12Mapper inst = Klarf12Mapper.getInstance();
    for (KlarfMappingRecord kmr : inst.mappingRecords) {
      System.out.println(kmr.toString());
    }

    Map<String, KlarfMappingRecord> map = inst.getMappingRecordsByKlarf12Key();
    System.out.println(map.containsKey("ENDOFFILE"));
    System.out.println(map.get("ENDOFFILE").klarfDataType());
  }

  public Map<String, KlarfMappingRecord> getMappingRecordsByKlarf12Key() {
    return mappingRecords.stream().collect(Collectors.toMap(kmr -> kmr.klarfKey12, kmr -> kmr));
  }

  public Map<String, KlarfDataLevel> getDataLevelByKlarf18Key() {
    return mappingRecords.stream()
        .collect(Collectors.toMap(kmr -> kmr.klarfKey18, kmr -> kmr.klarfDataLevel));
  }

  /**
   * key is the col name value is string array first element klarf 12 type and 2nd element klarf 18
   * type
   *
   * @param listName
   * @return
   */
  public Map<String, String[]> getColumnTypesForList(String listName) {
    // return listColTypes.stream().filter(e ->
    // e[0].equalsIgnoreCase(listName)).collect(Collectors.toMap(e -> e[1], e -> e[2]));//this might
    // not retain order
    return listColTypes.stream()
        .filter(e -> e[0].equalsIgnoreCase(listName))
        .collect(
            Collectors.toMap(
                e -> e[1],
                e -> new String[] {e[2], e[3]},
                (x, y) -> y,
                LinkedHashMap<String, String[]>::new));
  }

  public Map<String, String> getUnitConversion() {
    return unitConversion;
  }
}

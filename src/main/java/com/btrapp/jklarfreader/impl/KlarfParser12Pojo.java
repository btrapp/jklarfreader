package com.btrapp.jklarfreader.impl;

import com.btrapp.jklarfreader.KlarfParserIf18;
import com.btrapp.jklarfreader.objects.Klarf12Mapper;
import com.btrapp.jklarfreader.objects.Klarf12Mapper.KlarfDataLevel;
import com.btrapp.jklarfreader.objects.KlarfList;
import com.btrapp.jklarfreader.objects.KlarfRecord;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/** An implementation of the KlarfParterIf18 interface which emits a Plain Old Java Object */
public class KlarfParser12Pojo extends KlarfParser18Pojo implements KlarfParserIf18<KlarfRecord> {

  private Klarf12Mapper mapper;

  public KlarfParser12Pojo() throws Exception {
    super();
    mapper = Klarf12Mapper.getInstance();
  }

  @Override
  public Optional<KlarfRecord> build() {
    if (!this.recordStack.isEmpty()) {
      KlarfRecord kr = this.recordStack.getLast();
      return Optional.ofNullable(organizeKlarf(kr));
      // return Optional.ofNullable(this.recordStack.getLast());
    } else {
      System.err.println("RecordStack is not empty");
      return Optional.ofNullable(this.recordStack.getLast());
    }
  }

  /**
   * objects need to be at the right level - ex: lot attributes could be at file level - first stub
   * out the records needed FileRecord, LotRecord, WaferRecord, SummaryRecord and then attach
   * objects to the correct record and update the record id
   *
   * @param kr
   * @return
   */
  public KlarfRecord organizeKlarf(KlarfRecord kr) {
    // System.out.println("in org klarf");
    if (false) {
      return kr;
    }
    // System.out.println(kr.getName());
    KlarfRecord fileRecord =
        kr; // kr.findRecordsByName("FileRecord").stream().findFirst().orElseThrow();
    KlarfRecord lotRecord =
        fileRecord.findRecordsByName("LotRecord").stream().findFirst().orElseThrow();
    KlarfRecord waferRecord =
        lotRecord.findRecordsByName("WaferRecord").stream().findFirst().orElseThrow();

    //		List<KlarfRecord> lotRecords = fileRecord.findRecordsByName("LotRecord");
    //		List<KlarfRecord> waferRecords = fileRecord.findRecordsByName("WaferRecord");
    List<KlarfRecord> testRecords = waferRecord.findRecordsByName("TestRecord");

    Map<String, KlarfDataLevel> levelByKlarfKey = mapper.getDataLevelByKlarf18Key();
    // Map<String, KlarfDataLevel> levelByKlarfKey = new
    // HashMap<>();//klarfMapping.values().stream().collect(Collectors.toMap(kmr -> kmr.klarfKey18,
    // kmr -> kmr.klarfDataLevel));

    KlarfRecord fileRecordNew = new KlarfRecord(fileRecord.getName(), fileRecord.getId());
    KlarfRecord lotRecordNew = new KlarfRecord(lotRecord.getName(), lotRecord.getId());
    KlarfRecord waferRecordNew = new KlarfRecord(waferRecord.getName(), waferRecord.getId());

    //		List<KlarfRecord> lotRecordsNew = lotRecords.stream().map(oldRecord -> new
    // KlarfRecord(oldRecord.getName(), oldRecord.getId()))
    //				.collect(Collectors.toCollection(ArrayList::new));
    //		List<KlarfRecord> waferRecordsNew = waferRecords.stream().map(oldRecord -> new
    // KlarfRecord(oldRecord.getName(), oldRecord.getId()))
    //				.collect(Collectors.toCollection(ArrayList::new));
    List<KlarfRecord> testRecordsNew =
        testRecords.stream()
            .map(oldRecord -> new KlarfRecord(oldRecord.getName(), oldRecord.getId()))
            .collect(Collectors.toCollection(ArrayList::new));

    fileRecordNew.addRecord(lotRecordNew);
    lotRecordNew.addRecord(waferRecordNew);
    testRecordsNew.stream().forEach(testRecordNew -> waferRecordNew.addRecord(testRecordNew));

    orgKlarfRecord(
        fileRecord, levelByKlarfKey, fileRecordNew, lotRecordNew, waferRecordNew, testRecordsNew);
    return fileRecordNew;
  }

  private static void orgKlarfRecord(
      KlarfRecord krIn,
      Map<String, KlarfDataLevel> levelByKlarfKey,
      KlarfRecord fileRecordNew,
      KlarfRecord lotRecordNew,
      KlarfRecord waferRecordNew,
      List<KlarfRecord> testRecordsNew) {
    String recordName = krIn.getName();
    switch (recordName) {
      case "FileRecord", "LotRecord", "WaferRecord":
        for (Entry<String, List<String>> e : krIn.getFields().entrySet()) {
          KlarfDataLevel level = levelByKlarfKey.get(e.getKey());
          switch (level) {
            case File:
              // fileRecordNew.setField(e.getKey(), e.getValue(),
              // fileRecord.isQuotedField(e.getKey()));
              fileRecordNew.setField(e.getKey(), e.getValue(), krIn.isQuotedField(e.getKey()));
              break;
            case Lot:
              // lotRecordNew.setField(e.getKey(), e.getValue(),
              // lotRecord.isQuotedField(e.getKey()));
              lotRecordNew.setField(e.getKey(), e.getValue(), krIn.isQuotedField(e.getKey()));
              break;
            case Wafer:
              // waferRecordNew.setField(e.getKey(), e.getValue(),
              // waferRecord.isQuotedField(e.getKey()));
              waferRecordNew.setField(e.getKey(), e.getValue(), krIn.isQuotedField(e.getKey()));
              break;
            default:
              System.out.println(
                  "Look out, need to organize field " + e.getKey() + " " + e.getValue().toString());
              break;
          }
        }
        for (KlarfList kl : krIn.getLists()) {
          KlarfDataLevel level = levelByKlarfKey.get(kl.getName());
          switch (level) {
            case File:
              fileRecordNew.addList(kl);
              break;
            case Lot:
              lotRecordNew.addList(kl);
              break;
            case Wafer:
              waferRecordNew.addList(kl);
              break;
            default:
              System.out.println("Look out, need to organize list " + kl.getName());
              break;
          }
        }
        break;
      case "TestRecord":
        // System.out.println("process test record");
        KlarfRecord testRecordNew =
            testRecordsNew.stream()
                .filter(
                    tr -> tr.getName().equals(krIn.getName()) && tr.getId().equals(krIn.getId()))
                .findFirst()
                .orElseThrow();
        for (Entry<String, List<String>> e : krIn.getFields().entrySet()) {
          // System.out.println("  processing field " + e.getKey());
          KlarfDataLevel level = levelByKlarfKey.get(e.getKey());
          switch (level) {
            case File:
              // fileRecordNew.setField(e.getKey(), e.getValue(),
              // fileRecord.isQuotedField(e.getKey()));
              fileRecordNew.setField(e.getKey(), e.getValue(), krIn.isQuotedField(e.getKey()));
              break;
            case Lot:
              // lotRecordNew.setField(e.getKey(), e.getValue(),
              // lotRecord.isQuotedField(e.getKey()));
              lotRecordNew.setField(e.getKey(), e.getValue(), krIn.isQuotedField(e.getKey()));
              break;
            case Wafer:
              // waferRecordNew.setField(e.getKey(), e.getValue(),
              // waferRecord.isQuotedField(e.getKey()));
              waferRecordNew.setField(e.getKey(), e.getValue(), krIn.isQuotedField(e.getKey()));
              break;
            case Test:
              testRecordNew.setField(e.getKey(), e.getValue(), krIn.isQuotedField(e.getKey()));
              break;
            default:
              System.out.println(
                  "  Look out, need to organize field "
                      + e.getKey()
                      + " "
                      + e.getValue().toString()
                      + " level "
                      + level);
              break;
          }
        }
        for (KlarfList kl : krIn.getLists()) {
          KlarfDataLevel level = levelByKlarfKey.get(kl.getName());
          // System.out.println("  processing list " + kl.getName());
          switch (level) {
            case File:
              fileRecordNew.addList(kl);
              break;
            case Lot:
              lotRecordNew.addList(kl);
              break;
            case Wafer:
              waferRecordNew.addList(kl);
              break;
            case Test:
              testRecordNew.addList(kl);
              break;
            default:
              System.out.println("  Look out, need to organize list " + kl.getName());
              break;
          }
        }
        break;
      default:
        KlarfDataLevel level = levelByKlarfKey.get(recordName);
        if (level == null) level = KlarfDataLevel.Unsupported;
        // System.out.println("org default " + level + " for " + krIn.getName());
        switch (level) {
          case File:
            fileRecordNew.addRecord(krIn);
            break;
          case Lot:
            lotRecordNew.addRecord(krIn);
            break;
          case Wafer:
            waferRecordNew.addRecord(krIn);
            break;
          default:
            System.out.println(
                "Look out, need to organize record "
                    + recordName
                    + " level "
                    + level
                    + " current record "
                    + krIn.getName()
                    + " "
                    + krIn.getId());
            break;
        }
    }
    for (KlarfRecord kr : krIn.getRecords()) {
      orgKlarfRecord(
          kr, levelByKlarfKey, fileRecordNew, lotRecordNew, waferRecordNew, testRecordsNew);
    }
  }
}

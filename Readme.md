# jKlarfReader

This project provides a reader for a 1.8 format Klarf file.

This is not in any way endorsed by KLA-Tencor, who do provide official commercial support, documentation, 
and libraries.  If you need/want the real deal, they're very good at what they do and you should contact them.

## Important Files

### KlarfParser18Pojo 

The KlarfParser18Pojo class is a simple java representation of the Klarf file layout, with KlarfRecord, KlarfList, and fields with each object. 

```
Optional<KlarfRecord> klarf = KlarfReader.parseKlarf(new KlarfParser18Pojo(), fis);
if (klarf.isPresent()) {
  for (KlarfRecord lotRecord: klarf.get().findRecordsByName("LotRecord")) {
    for (KlarfRecord waferRecord: lotRecord.findRecordsByName("WaferRecord")) {
      KlarfList defects = waferRecord.findListByName("DefectList").orElse(new KlarfList());
    }
  }
}
```

KlarfParser18Pojo also seralizes nicely to Json using Jackson's ObjectMapper, if you so desire.

## KlarfParerIf18

You can also implement the KlarfParerIf18 interface using your own logic if you're looking for a more flexible way
to process Klarfs.  If you only want to keep certain attributes, or if you want to be able to process data without having to 
wait for the entire klarf to be loaded in memory, this may be a more appealing choice for you.  (This of this as like SAX based XML 
parsing instead of DOM parsing)


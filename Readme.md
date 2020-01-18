# jKlarfReader

This project provides a reader for a 1.8 format Klarf file.

This work is based on my own black-box, reverse engineering of the format, and is not in any way endorsed by KLA-Tencor, who do provide official commercial support, documentation, 
and libraries.  If you need/want the real deal, they're very good at what they do and you should contact them.  (https://www.kla-tencor.com/)

## Important Files

### KlarfParser18Pojo 

The KlarfParser18Pojo class is a simple plain-old-java object representing the Klarf file layout, with KlarfRecord, KlarfList, and fields with each object. 

```
Optional<KlarfRecord> klarf = KlarfReader.parseKlarf(new KlarfParser18Pojo(), anInputStream);
if (klarf.isPresent()) {
    KlarfRecord klarfRecord = klarf.get();
    for (KlarfRecord lotRecord : klarfRecord.findRecordsByName("LotRecord")) {
        for (KlarfRecord waferRecord : lotRecord.findRecordsByName("WaferRecord")) {
            for (KlarfList defectList : waferRecord.findListsByName("DefectList")) {
                defectCount += defectList.size();
            }
        }
    }
}
```

KlarfParser18Pojo also serializes nicely to JSON using Jackson's ObjectMapper, if you so desire.

## KlarfParserIf18

You can also implement the KlarfParserIf18 interface using your own logic if you're looking for a more flexible way
to process Klarfs.  If you only want to keep certain attributes, or if you want to be able to process data without having to 
wait for the entire Klarf to be loaded in memory, this may be a more appealing choice for you.  (You can this of this as acting like SAX based XML 
parsing instead of DOM parsing)


## Maven Artifact
```
<dependency>
  <groupId>com.github.btrapp</groupId>
  <artifactId>jklarfreader</artifactId>
  <version>0.9.2</version>
</dependency>
```


# jKlarfReader

This project provides a reader for a 1.8 format Klarf file.

This work is based on my own black-box, reverse engineering of the format, and is not in any way endorsed by KLA-Tencor, who do provide official commercial support, documentation, 
and libraries.  If you need/want the real deal, they're very good at what they do and you should contact them.  (https://www.kla-tencor.com/)

Versions 0.9.22 and up required Java 17 or better.

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
wait for the entire Klarf to be loaded in memory, this may be a more appealing choice for you.  (You can think of this as acting like SAX based XML 
parsing instead of DOM parsing)

## KlarfWriter18

If you're parsing to the KlarfRecord objects, you can write the data back to a new Klarf using the KlarfWriter18 class.


## Command-line utilities

###Klarf-to-Json
There's a simple command line utility to validate a klarf, and optionally convert it to json.  Here's an example of how to build 
a fat-jar (containing required dependencies) and run it from the command line:

```
mvn clean package
java -jar target/jklarfreader-0.9.30-SNAPSHOT-jar-with-dependencies.jar /path/to/klarf.klarf (optional: /path/to/klarf.json)
```

###Klarf-to-Image
There's a command line utility to render a klarf to a png (or jpeg).  The image is relatively simple, ideal for feeding into a ML model.  Example usage:

```
mvn clean package
java -cp target/jklarfreader-0.9.30-SNAPSHOT-jar-with-dependencies.jar com.btrapp.jklarfreader.util.KlarfToImage -klarf /path/to/my/klarf.KLAR -image /path/to/image.png -imgSizePx 224
```

## Maven Artifact
```
<dependency>
  <groupId>com.github.btrapp</groupId>
  <artifactId>jklarfreader</artifactId>
  <version>0.9.30</version>
</dependency>
```


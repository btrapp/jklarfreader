package com.btrapp.jklarfreader.objects;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KlarfWriter18 {

  public void writeKlarf(KlarfRecord krecord, Writer writer) throws IOException {
    writeRecord(krecord, writer, 0);
    writer.write("EndOfFile;\n");
    writer.flush();
  }

  private void writeRecord(KlarfRecord krecord, Writer writer, int indent) throws IOException {
    writer.write(spaces(indent) + "Record " + krecord.getName());
    if (krecord.getId() != null && (!krecord.getId().isEmpty())) {
      // The ID field may be missing, and some loaders don't like Record Name of blank quotes like  "" 
      writer.write(" \"" + krecord.getId() + "\"");
    }
    writer.write(" {\n");
    for (String fieldName : krecord.getFields().keySet()) {
      writer.write(writeField(krecord, fieldName, indent));
    }
    for (KlarfList list : krecord.getLists()) {
      writer.write(writeList(list, indent));
    }
    for (KlarfRecord innerRecord : krecord.getRecords()) {
      writeRecord(innerRecord, writer, (indent + 1));
    }
    writer.write(spaces(indent) + "}\n");
  }

  private String writeList(KlarfList list, int indent) {
    StringBuilder sb = new StringBuilder();
    String pad = spaces(indent);
    sb.append(pad + " List " + list.getName() + " {\n");
    sb.append(pad + "  Columns " + list.getColumnNames().size() + " {");
    for (int i = 0; i < list.getColumnNames().size(); i++) {
      if (i > 0) sb.append(",");
      sb.append(" " + list.getColumnTypes().get(i));
      sb.append(" " + list.getColumnNames().get(i));
    }
    sb.append("}\n"); // End columns
    sb.append(pad + "  Data " + list.size() + " {\n");
    for (int i = 0; i < list.size(); i++) {
      List<Object> row = list.asRow(i);
      sb.append(pad + "  ");
      for (int j = 0; j < row.size(); j++) {
        String colType = list.getColumnTypes().get(j);
        if ("int32".equals(colType) || "float".equals(colType)) {
          // No quotes
          sb.append(" " + row.get(j));
        } else if ("ImageList".equalsIgnoreCase(colType)) {
          // Special ImageList format.
          sb.append(" " + printImageList(row.get(j)));
        } else {
          sb.append(" \"" + row.get(j) + "\"");
        }
      }
      sb.append(";\n");
    }
    sb.append(pad + "  }\n"); // End Data
    sb.append(pad + " }\n"); // End List
    return sb.toString();
  }

  private String printImageList(Object imageListObject) {
    StringBuilder sb = new StringBuilder();
    List<List<String>> imageList = (List<List<String>>) imageListObject;
    if (imageList.isEmpty()) {
      sb.append(" N");
    } else {
      sb.append(" Images " + imageList.size() + " {");
      for (int i = 0; i < imageList.size(); i++) {
        if (i > 0) sb.append(", "); // Separate rows with ,s
        List<String> innerRow = imageList.get(i);
        for (String o : innerRow) {
          if (o.matches("\\d")) {
            // Don't quote simple numbers.
            sb.append(" " + o);
          } else {
            sb.append(" \"" + o + "\"");
          }
        }

        sb.append("}");
      }
    }
    return sb.toString();
  }

  private String writeField(KlarfRecord krecord, String fieldName, int indent) {
    List<String> fieldValues = new ArrayList<>(krecord.getFields().get(fieldName));
    boolean quoted = krecord.isQuotedField(fieldName);
    StringBuilder sb = new StringBuilder();
    sb.append(spaces(indent));
    sb.append(" Field " + fieldName + " " + fieldValues.size() + " {");
    if (quoted) {
      fieldValues = fieldValues.stream().map(s -> "\"" + s + "\"").toList();
    }
    sb.append(fieldValues.stream().collect(Collectors.joining(",")));
    sb.append("}\n");
    return sb.toString();
  }

  private String spaces(int indent) {
    return Stream.generate(() -> " ").limit(indent).collect(Collectors.joining());
  }
}

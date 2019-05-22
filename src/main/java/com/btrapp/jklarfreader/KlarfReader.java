package com.btrapp.jklarfreader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.btrapp.jklarfreader.objects.KlarfReader18;

public class KlarfReader {
	public static enum KlarfFormat {
		V1_0, V1_2, V1_8
	}

	public static <T> Optional<T> parseKlarf(KlarfParserIf18<T> parser, InputStream is) throws Exception {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
			return new KlarfReader18<T>(parser).readKlarf(br);
		} catch (IOException e) {
			throw (e);
		}
	}
	//	public boolean parseKlarf(KlarfParserIf12 parser, InputStream is) throws Exception {
	//		try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
	//			new KlarfReader12().readKlarf(parser, br);
	//		} catch (IOException e) {
	//			throw (e);
	//		}
	//		return false;
	//	}

	public static KlarfFormat findKlarfFormat(BufferedReader br) throws IOException {
		//Record FileRecord  "1.8"
		//FileVersion 1 2;
		Pattern pattern1_8 = Pattern.compile("Record\\s+FileRecord.*1\\.8.*");
		Pattern pattern1_2 = Pattern.compile("FileVersion\\s+1\\s+2.*");
		Pattern pattern1_0 = Pattern.compile("FileVersion\\s+1\\s+0.*");
		Matcher m;
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			m = pattern1_8.matcher(line);
			if (m.matches()) {
				return KlarfFormat.V1_8;
			}
			m = pattern1_2.matcher(line);
			if (m.matches()) {
				return KlarfFormat.V1_2;
			}
			m = pattern1_0.matcher(line);
			if (m.matches()) {
				return KlarfFormat.V1_0;
			}
		}
		return null;

	}
}

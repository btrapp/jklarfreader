package com.btrapp.jklarfreader.objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class KlarfTokenizerTestCase {

	@Test
	public void testIntError() {
		assertThrows(KlarfException.class,
				() -> {
					KlarfTokenizer kt = new KlarfTokenizer(new ByteArrayInputStream("A".getBytes()));
					kt.nextIntVal();
					kt.close();
				});
	}

	@Test
	public void testQuotedStringsWork() throws Exception {
		try (KlarfTokenizer kt = new KlarfTokenizer(new ByteArrayInputStream("A \"B;\" \"C{}\" \";D\"".getBytes()))) {
			List<String> tokens = new ArrayList<>();
			while (kt.nextToken()) {
				tokens.add(kt.val());
			}
			int i = 0;
			assertEquals("A", tokens.get(i++));
			assertEquals("B;", tokens.get(i++));
			assertEquals("C{}", tokens.get(i++));
			assertEquals(";D", tokens.get(i++));
		}
	}

	@Test
	public void testTokening() throws Exception {
		try (KlarfTokenizer kt = new KlarfTokenizer(new ByteArrayInputStream("A { \"Spaces\" \"Space, Comma\", } B  \t {1,2,3}".getBytes()))) {
			List<String> tokens = new ArrayList<>();
			while (kt.nextToken()) {
				tokens.add(kt.val());
			}
			int i = 0;
			assertEquals("A", tokens.get(i++));
			assertEquals("{", tokens.get(i++));
			assertEquals("Spaces", tokens.get(i++));
			assertEquals("Space, Comma", tokens.get(i++));
			assertEquals(",", tokens.get(i++));
			assertEquals("}", tokens.get(i++));
			assertEquals("B", tokens.get(i++));
			assertEquals("{", tokens.get(i++));
			assertEquals("1", tokens.get(i++));
			assertEquals(",", tokens.get(i++));
			assertEquals("2", tokens.get(i++));
			assertEquals(",", tokens.get(i++));
			assertEquals("3", tokens.get(i++));
			assertEquals("}", tokens.get(i++));
			//System.out.println(tokens.toString());
		}

	}

	@Test
	public void testReadingEmptyQuotedStrings() throws Exception {
		try (KlarfTokenizer kt = new KlarfTokenizer(new ByteArrayInputStream("A \"\" B\t\"C\" D \"\"".getBytes()))) {
			List<String> tokens = new ArrayList<>();
			while (kt.nextToken()) {
				tokens.add(kt.val());
			}
			int i = 0;
			assertEquals("A", tokens.get(i++));
			assertEquals("", tokens.get(i++));
			assertEquals("B", tokens.get(i++));
			assertEquals("C", tokens.get(i++));
			assertEquals("D", tokens.get(i++));
			assertEquals("", tokens.get(i++));
			//System.out.println(tokens.toString());
		}
	}

	@Test
	public void testTokeningJustCommas() throws Exception {
		try (KlarfTokenizer kt = new KlarfTokenizer(new ByteArrayInputStream("A,B,C,\"D E\",\"F,G\"".getBytes()))) {
			List<String> tokens = new ArrayList<>();
			while (kt.nextToken()) {
				tokens.add(kt.val());
			}
			int i = 0;
			assertEquals("A", tokens.get(i++));
			assertEquals(",", tokens.get(i++));
			assertEquals("B", tokens.get(i++));
			assertEquals(",", tokens.get(i++));
			assertEquals("C", tokens.get(i++));
			assertEquals(",", tokens.get(i++));
			assertEquals("D E", tokens.get(i++));
			assertEquals(",", tokens.get(i++));
			assertEquals("F,G", tokens.get(i++));
		}

	}
}

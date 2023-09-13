package com.btrapp.jklarfreader.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import com.btrapp.jklarfreader.KlarfReader;
import com.btrapp.jklarfreader.impl.KlarfParser18Pojo;
import com.btrapp.jklarfreader.objects.KlarfList;
import com.btrapp.jklarfreader.objects.KlarfRecord;

/**
 * Assumptions: Each klarf has only one wafer.
 * Klarf has expected FileRecord->LotRecord->WaferRecord structure
 * 
 */
public class KlarfToImage {
	//Provided
	enum CliArg {
		klarf, image, drawNotch, drawChipGrid, drawWaferOutline, imgSizePx, dotSizePx, imgType
	}

	public static void main(String[] args) {
		KlarfImageOptions kio = parseOptionsFromArgs(args);

		KlarfToImage.drawKlarf(kio);
	}

	private static KlarfImageOptions parseOptionsFromArgs(String[] args) {
		KlarfImageOptions kio = new KlarfImageOptions();
		Map<String, Consumer<String>> parms = new TreeMap<>();
		Map<String, String> descriptions = new TreeMap<>();
		for (CliArg ca : CliArg.values()) {
			String cliKey = "-" + ca.name();
			switch (ca) {
			case dotSizePx:
				parms.put(cliKey, new Consumer<String>() {
					@Override
					public void accept(String arg) {
						kio.setDotSizePx(Integer.valueOf(arg));
					}
				});
				descriptions.put(cliKey, "[integer] The diameter, in pixels, of how big each defect dot should be.  Default is " + kio.dotSizePx);
				break;
			case drawChipGrid:
				parms.put(cliKey, new Consumer<String>() {
					@Override
					public void accept(String arg) {
						kio.setDrawChipGrid("true".equalsIgnoreCase(arg));
					}
				});
				descriptions.put(cliKey, "[true|false] If chip outlines should be drawn.  Default is " + kio.drawChipGrid);
				break;
			case drawNotch:
				parms.put(cliKey, new Consumer<String>() {
					@Override
					public void accept(String arg) {
						kio.setDrawNotch("true".equalsIgnoreCase(arg));
					}
				});
				descriptions.put(cliKey, "[true|false] If the notch should be drawn.  Default is " + kio.drawNotch);
				break;
			case drawWaferOutline:
				parms.put(cliKey, new Consumer<String>() {
					@Override
					public void accept(String arg) {
						kio.setDrawWaferOutline("true".equalsIgnoreCase(arg));
					}
				});
				descriptions.put(cliKey, "[true|false] If the wafer outline circle should be drawn. Default is " + kio.drawWaferOutline);
				break;
			case imgSizePx:
				parms.put(cliKey, new Consumer<String>() {
					@Override
					public void accept(String arg) {
						kio.setImgSizePx(Integer.valueOf(arg));
					}
				});
				descriptions.put(cliKey, "[integer] How big the wafermap image should be.  Default is " + kio.imgSizePx);
				break;
			case imgType:
				parms.put(cliKey, new Consumer<String>() {
					@Override
					public void accept(String arg) {
						kio.setImgType(arg);
					}
				});
				descriptions.put(cliKey, "[png|jpg] Output image format.  Default is " + kio.imgType);
				break;
			case klarf:
				parms.put(cliKey, new Consumer<String>() {
					@Override
					public void accept(String arg) {
						kio.setKlarfIn(new File(arg));
					}
				});
				descriptions.put(cliKey, "[file] (REQUIRED) file name (and path if required) to the 1.8 version klarf to read");
				break;
			case image:
				parms.put(cliKey, new Consumer<String>() {
					@Override
					public void accept(String arg) {
						kio.setPngOut(new File(arg));
					}
				});
				descriptions.put(cliKey, "[file] (REQUIRED) file name (and path if required) of the image to create");
				break;
			}
		}
		if (args.length == 0) {
			System.err.println("Arguments expected!  Here is a list of possible values:");
			printUsage(descriptions);
			System.exit(1);
		}
		if (args.length % 2 != 0) {
			System.err.println("An even number of arguments is expected.");
			System.exit(1);
		}
		try {
			for (int i = 0; i < args.length; i += 2) {
				String cliKey = args[i];
				String cliValue = args[i + 1];
				boolean parmMatchedSomething = false;
				for (var e : parms.entrySet()) {
					if (e.getKey().equalsIgnoreCase(cliKey)) {
						e.getValue().accept(cliValue);
						parmMatchedSomething = true;
					}
				}
				if (!parmMatchedSomething) {
					System.out.println("Warning - arg '" + cliKey + "' didn't match any known argument.");
				}
			}
		} catch (Exception ex) {
			System.err.println("Arg parsing error:");
			ex.printStackTrace();
			System.err.println("Arguments expected!  Here is a list of possible values:");
			printUsage(descriptions);
			System.exit(1);
		}

		//Check some required fields
		if (kio.getKlarfIn() == null) {
			System.err.println("Please specify the Klarf to read");
			printUsage(descriptions);
			System.exit(1);
		}
		if (kio.getPngOut() == null) {
			System.err.println("Please specify the PNG file to create");
			printUsage(descriptions);
			System.exit(1);
		}
		if (kio.getKlarfIn().canRead() == false) {
			System.err.println("Can't read klarf '" + kio.getKlarfIn().getAbsolutePath() + "'");
			System.exit(1);
		}

		return kio;
	}

	private static void printUsage(Map<String, String> argDescriptions) {
		System.out.println("Valid arguments: ");
		for (var e : argDescriptions.entrySet()) {
			System.out.println("  " + e.getKey() + " : " + e.getValue());
		}
	}

	public static final class KlarfImageOptions {
		private File klarfIn;
		private File pngOut;
		private boolean drawChipGrid = false;
		private boolean drawWaferOutline = true;
		private boolean drawNotch = true;
		private int imgSizePx = 226;
		private int dotSizePx = 6;
		private String imgType = "png";

		public boolean isDrawChipGrid() {
			return drawChipGrid;
		}

		public KlarfImageOptions setDrawChipGrid(boolean drawChipGrid) {
			this.drawChipGrid = drawChipGrid;
			return this;
		}

		public boolean isDrawWaferOutline() {
			return drawWaferOutline;
		}

		public KlarfImageOptions setDrawWaferOutline(boolean drawWaferOutline) {
			this.drawWaferOutline = drawWaferOutline;
			return this;
		}

		public boolean isDrawNotch() {
			return drawNotch;
		}

		public KlarfImageOptions setDrawNotch(boolean drawNotch) {
			this.drawNotch = drawNotch;
			return this;
		}

		public int getImgSizePx() {
			return imgSizePx;
		}

		public KlarfImageOptions setImgSizePx(int imgSizePx) {
			this.imgSizePx = imgSizePx;
			return this;
		}

		public int getDotSizePx() {
			return dotSizePx;
		}

		public KlarfImageOptions setDotSizePx(int dotSizePx) {
			this.dotSizePx = dotSizePx;
			return this;
		}

		public File getKlarfIn() {
			return klarfIn;
		}

		public KlarfImageOptions setKlarfIn(File klarfIn) {
			this.klarfIn = klarfIn;
			return this;
		}

		public File getPngOut() {
			return pngOut;
		}

		public KlarfImageOptions setPngOut(File pngOut) {
			this.pngOut = pngOut;
			return this;
		}

		public String getImgType() {
			return imgType;
		}

		public void setImgType(String imgType) {
			this.imgType = imgType;
		}

	}

	private static final class KlarfImageDrawer {
		//Calculated
		private int imgSizePx;
		private double imgScale;
		private int dotSizePx = 4;
		protected BufferedImage bi = null;
		private Graphics2D g2d;
		private KlarfSetupInfo ksi;

		public KlarfImageDrawer(KlarfImageOptions kio, KlarfSetupInfo ksi) {
			this.imgSizePx = kio.imgSizePx;
			this.ksi = ksi;
			imgScale = ksi.getWaferDiameter() / (double) imgSizePx;
			bi = new BufferedImage(imgSizePx, imgSizePx, BufferedImage.TYPE_INT_RGB);
			g2d = bi.createGraphics();
			g2d.setColor(Color.WHITE);
			g2d.fill(new Rectangle2D.Float(0, 0, imgSizePx, imgSizePx)); //Fill it with white

			if (kio.isDrawWaferOutline()) {
				Ellipse2D.Float outline = new Ellipse2D.Float(0, 0, imgSizePx, imgSizePx);
				g2d.setColor(Color.black);
				g2d.draw(outline);
			}
			if (kio.isDrawNotch()) {
				IntXY notchPt = scaleToPx(new DoubleXY(0.0, -(ksi.getWaferDiameter() / 2.0)));
				Line2D.Float n1 = new Line2D.Float(notchPt.getX() - 4, notchPt.getY(), notchPt.getX(), notchPt.getY() - 4);
				Line2D.Float n2 = new Line2D.Float(notchPt.getX() + 4, notchPt.getY(), notchPt.getX(), notchPt.getY() - 4);
				Polygon rect = new Polygon();
				rect.addPoint(notchPt.getX() - 3, notchPt.getY());
				rect.addPoint(notchPt.getX(), notchPt.getY() - 3);
				rect.addPoint(notchPt.getX() + 3, notchPt.getY());
				rect.addPoint(notchPt.getX(), notchPt.getY() + 3);
				rect.addPoint(notchPt.getX() - 3, notchPt.getY());
				g2d.setColor(Color.BLACK);
				g2d.draw(n1);
				g2d.draw(n2);
				g2d.setColor(Color.WHITE);
				g2d.fill(rect);
			}
		}

		private void drawChips(Collection<IntXY> chipIds) {
			g2d.setColor(Color.black);
			DoubleXY noDefectOffset = new DoubleXY(0, 0); //We're drawing chips not defects
			for (IntXY chipId : chipIds) {
				DoubleXY llxyD = mapChipGridToWaferLocation(ksi.sampleCenterLocation, ksi.diePitch, ksi.dieOrigin, chipId, noDefectOffset);
				IntXY llxy = scaleToPx(llxyD);
				IntXY urxy = scaleToPx(new DoubleXY(llxyD.x + ksi.diePitch.x, llxyD.y + ksi.diePitch.y));
				int xpx = llxy.x;
				int ypx = urxy.y; // Note - UR!
				int w = urxy.x - llxy.x;
				int h = -(urxy.y - llxy.y); // Scaling flips the sign on us
				Rectangle2D.Float ret = new Rectangle2D.Float(xpx, ypx, w, h);
				g2d.setColor(Color.lightGray);
				g2d.draw(ret);
			}
		}

		private void drawDefects(double[] xUm, double[] yUm, List<IntXY> chipIds) {

			g2d.setColor(Color.black);
			int dotSizeHalf = dotSizePx / 2;
			for (int i = 0; i < xUm.length; i++) {
				DoubleXY defectXYRelUm = new DoubleXY(xUm[i], yUm[i]);
				IntXY chipId = chipIds.get(i);
				DoubleXY waferUm = mapChipGridToWaferLocation(ksi.sampleCenterLocation, ksi.diePitch, ksi.dieOrigin, chipId, defectXYRelUm);
				IntXY xyPx = scaleToPx(waferUm);
				//System.out.println("I " + i + " is " + xUm[i] + "," + yUm[i] + " in Wafer=" + waferUm.toString() + " in PX=" + xyPx.toString());
				Ellipse2D.Double dot = new Ellipse2D.Double(xyPx.getX() - dotSizeHalf, xyPx.getY() - dotSizeHalf, dotSizePx, dotSizePx);
				g2d.fill(dot);
			}
		}

		private IntXY scaleToPx(DoubleXY waferXY) {
			int x = (int) (Math.round((waferXY.getX() / imgScale) + (imgSizePx / 2.0)));
			int y = imgSizePx - (int) Math.round((waferXY.getY() / imgScale) + (imgSizePx / 2.0));
			return new IntXY(x, y);
		}

		public double getImgScale() {
			return imgScale;
		}

		public void setImgScale(double imgScale) {
			this.imgScale = imgScale;
		}

		public BufferedImage getBi() {
			return bi;
		}

		public void setBi(BufferedImage bi) {
			this.bi = bi;
		}

		public Graphics2D getG2d() {
			return g2d;
		}

		public void setG2d(Graphics2D g2d) {
			this.g2d = g2d;
		}
	}

	public static final class KlarfSetupInfo {
		//All values in um
		private int waferDiameter = 300_000;
		private DoubleXY diePitch = new DoubleXY(0, 0);
		private DoubleXY dieOrigin = new DoubleXY(0, 0);
		private DoubleXY sampleCenterLocation = new DoubleXY(0, 0);

		public KlarfSetupInfo(int waferDiameter, DoubleXY diePitch, DoubleXY dieOrigin, DoubleXY sampleCenterLocation) {
			super();
			this.waferDiameter = waferDiameter;
			this.diePitch = diePitch;
			this.dieOrigin = dieOrigin;
			this.sampleCenterLocation = sampleCenterLocation;
		}

		public int getWaferDiameter() {
			return waferDiameter;
		}

		public DoubleXY getDiePitch() {
			return diePitch;
		}

		public DoubleXY getDieOrigin() {
			return dieOrigin;
		}

		public DoubleXY getSampleCenterLocation() {
			return sampleCenterLocation;
		}

	}

	public static void drawKlarf(KlarfImageOptions kio) {
		try (FileInputStream fis = new FileInputStream(kio.klarfIn)) {
			KlarfRecord klarf = KlarfReader.parseKlarf(new KlarfParser18Pojo(), fis).orElse(null);
			if (klarf == null) {
				System.err.println("Unable to read klarf");
				System.exit(1);
			}
			KlarfRecord lotRec = klarf.findRecordsByName("LotRecord").stream().findFirst().orElseThrow();
			KlarfRecord waferRec = lotRec.findRecordsByName("WaferRecord").stream().findFirst().orElseThrow();
			int waferDiameterUm = Integer.valueOf(lotRec.findField("SampleSize").get(0)) / 1000;
			DoubleXY diePitch = nmToUmXYField(lotRec.findField("DiePitch"));
			DoubleXY dieOrigin = nmToUmXYField(waferRec.findField("DieOrigin"));
			DoubleXY sampleCenterLocation = nmToUmXYField(waferRec.findField("SampleCenterLocation"));

			KlarfSetupInfo ksi = new KlarfSetupInfo(waferDiameterUm, diePitch, dieOrigin, sampleCenterLocation);

			KlarfImageDrawer kii = new KlarfImageDrawer(kio, ksi); //Gets bare wafer ready
			if (kio.isDrawChipGrid()) {
				//Only take the hit to read the chip grid if required.  A klarf can have many test records, but we only need the unique set
				List<IntXY> uniqueChipIds = waferRec.findRecordsByName("TestRecord").stream() //A wafer can have several tests
						.flatMap(testRec -> testRec.findListsByName("SampleTestPlanList").stream()) // Each test may have a SampleTestPlan
						.flatMap(stpList -> parseChipIdsFromKlarfList(stpList).stream())
						.distinct()
						.toList();
				kii.drawChips(uniqueChipIds);
			}
			//Now draw defects
			waferRec.findListsByName("DefectList").stream().findFirst().ifPresent(defList -> {
				double[] xrelUm = defList.getColumn("XREL").stream().mapToDouble(d -> ((Integer) d).doubleValue() / 1000.0).toArray();
				double[] yrelUm = defList.getColumn("YREL").stream().mapToDouble(d -> ((Integer) d).doubleValue() / 1000.0).toArray();
				List<IntXY> chipIds = parseChipIdsFromKlarfList(defList);
				kii.drawDefects(xrelUm, yrelUm, chipIds);
			});

			ImageIO.write(kii.getBi(), kio.imgType, kio.pngOut);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

	}

	private static List<IntXY> parseChipIdsFromKlarfList(KlarfList list) {
		List<String> colNames = list.getColumnNames();
		if ((colNames.contains("XINDEX") && colNames.contains("YINDEX")) == false) {
			//We are missing critical columns.
			return Collections.emptyList();
		}
		int[] chipX = list.getColumn("XINDEX").stream().mapToInt(d -> ((Integer) d)).toArray();
		int[] chipY = list.getColumn("YINDEX").stream().mapToInt(d -> ((Integer) d)).toArray();
		List<IntXY> chipIds = new ArrayList<>(chipX.length);
		for (int i = 0; i < chipX.length; i++) {
			chipIds.add(new IntXY(chipX[i], chipY[i]));
		}
		return chipIds;
	}

	private static DoubleXY nmToUmXYField(List<String> fieldContents) {
		if (fieldContents != null && fieldContents.size() == 2)
			return new DoubleXY(Double.parseDouble(fieldContents.get(0)) / 1000.0, Double.parseDouble(fieldContents.get(1)) / 1000.0);
		return new DoubleXY(0, 0);
	}

	/**
	 * 
	 * @param sampleCenterLocationUm
	 * @param diePitchUm
	 * @param originIndex
	 * @param chipIdXY
	 * @param defChipLocXYUm
	 *            a relative (XREL/YREL from klarf) offset from chip LLX in um (like the defect's XREL/YREL)
	 * @return
	 */
	protected static DoubleXY mapChipGridToWaferLocation(DoubleXY sampleCenterLocationUm, DoubleXY diePitchUm, DoubleXY originIndex, IntXY chipIdXY, DoubleXY defChipLocXYUm) {
		double x = defChipLocXYUm.getX() + (-sampleCenterLocationUm.getX()) + (diePitchUm.getX() * (chipIdXY.getX() - originIndex.getX()));
		double y = defChipLocXYUm.getY() + (-sampleCenterLocationUm.getY()) + (diePitchUm.getY() * (chipIdXY.getY() - originIndex.getY()));
		return new DoubleXY(x, y);
	}

	private static final class DoubleXY {
		private double x;
		private double y;

		public DoubleXY(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		@Override
		public String toString() {
			return x + "," + y;
		}
	}

	private static final class IntXY {
		private int x;
		private int y;

		public IntXY(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		@Override
		public String toString() {
			return x + "," + y;
		}
	}
}

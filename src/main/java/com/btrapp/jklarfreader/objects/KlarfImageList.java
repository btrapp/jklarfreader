package com.btrapp.jklarfreader.objects;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Use this if you are supporting a (filePath,imgType,img,imgLabel) style record
 * ex (  {"file01.jpg","JPG",1,"Patch"},{"file02.jpg","JPG",1,"Class-Topo"}  )
 */
public class KlarfImageList {
	public record KlarfImageRow(String filePath, String imgType, int imgIndex, String imgLabel) {
		@Override
		public String toString() {
			//a,b,c,d,e -> "a","b","c",d,e
			return "\""+filePath+"\" \""+imgType+"\" "+imgIndex+" \""+imgLabel+"\" ";
		}
	}
	private List<KlarfImageRow> images = Collections.emptyList();
	@Override
	public String toString() {
		if (images.isEmpty())
			return "N";
		return "Images "+images.size()+" {"+images.stream().map(KlarfImageRow::toString).collect(Collectors.joining(", "))+"}";
	}
	public List<KlarfImageRow> getImages() {
		return images;
	}
	public void setImages(List<KlarfImageRow> images) {
		this.images = images;
	}
}

package com.hackathon.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.common.collect.ImmutableList;

@Controller
public class ReceiptServiceImpl implements ReceiptService	{
	
	
	@Autowired
	Vision visionService;
	
	@Override
	public String processReceiptImage(MultipartFile file) {

		ImmutableList.Builder<AnnotateImageRequest> requests = ImmutableList.builder();
		Image image = new Image();
		try {
			image.encodeContent(file.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		AnnotateImageRequest air = new AnnotateImageRequest();
		air.setImage(image);
		air.setFeatures(ImmutableList.of(new Feature().setType("TEXT_DETECTION").setMaxResults(10)));
		requests.add(air);

		
		BatchAnnotateImagesResponse batchResponse = null;
		try {
			Vision.Images.Annotate annotate = visionService.images().annotate(new BatchAnnotateImagesRequest().setRequests(requests.build()));
			batchResponse = annotate.execute();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assert batchResponse.getResponses().size() == 1;
		AnnotateImageResponse response = batchResponse.getResponses().get(0);
		
		List<String> descriptions = new ArrayList<>();
		

		Map<Integer, StringBuffer> map = new LinkedHashMap<>();
		
		for (EntityAnnotation s : response.getTextAnnotations()) 
		{
			if (map.isEmpty()) {
				StringBuffer buffer = new StringBuffer();
				buffer.append(s.getDescription());
				map.put(s.getBoundingPoly().getVertices().get(0).getY(), buffer);
			} else {
				int y = s.getBoundingPoly().getVertices().get(0).getY();
				if (map.containsKey(y)) {
					map.get(y).append(" " + s.getDescription());
				} else {
					int closest = findClosestKey(y, map);
					if (closest == -1) {
						StringBuffer buffer = new StringBuffer();
						buffer.append(s.getDescription());
						map.put(y, buffer);
					} else {
						map.get(closest).append(" " + s.getDescription());
					}
				}
			}
		}
		
		map.values().stream().filter(s -> s.length() < 50 && s.length() > 10).forEach(s -> System.out.println(s));

		return null;
	}
	
	private static class Pair {
		int index;
		long distance; 
		
		Pair(int index, long distance) {
			this.index = index;
			this.distance = distance;		
		}
	}

	private static int findClosestKey(int y, Map<Integer, StringBuffer> map) {
		Optional<Pair> p = map.keySet().stream().map(key -> new Pair(key, Math.abs(y - key))).min(new Comparator<Pair>() {
			@Override
			public int compare(Pair o1, Pair o2) {
				long diff = o1.distance - o2.distance;
				return diff == 0l ? 0 : (diff > 0) ? 1 : -1; 
			}
		});
		return p.get().distance > 4l ? -1 : p.get().index;
	}
	
	
	

}

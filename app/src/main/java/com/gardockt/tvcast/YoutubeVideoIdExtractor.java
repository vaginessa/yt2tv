package com.gardockt.tvcast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YoutubeVideoIdExtractor {

	private final static String youtubeVideoIdAllowedCharsRegexString = "a-zA-Z0-9\\-_";
	private final static String youtubeVideoIdRegexString = String.format("[%s]{11}",
			youtubeVideoIdAllowedCharsRegexString);

	private final static Pattern youtubeVideoIdPattern = Pattern.compile(youtubeVideoIdRegexString);

	private final static Pattern videoIdParamPattern = Pattern.compile(String.format("[?&]v=(%s)",
			youtubeVideoIdRegexString));
	private final static Pattern videoIdPathPattern = Pattern.compile(String.format("/(%s)(?:[^%s/][^/]*)?$",
			youtubeVideoIdRegexString, youtubeVideoIdAllowedCharsRegexString));

	public static String extract(String data) throws VideoIdNotFoundException {
		// only video ID entered
		if (isValidYoutubeVideoId(data)) {
			return data;
		}

		// get from URL query parameter
		Matcher videoIdParamMatcher = videoIdParamPattern.matcher(data);
		if (videoIdParamMatcher.find()) {
			String videoIdParam = videoIdParamMatcher.group(1);
			if (videoIdParam != null && isValidYoutubeVideoId(videoIdParam)) {
				return videoIdParam;
			}
		}

		// get from URL path
		Matcher videoIdPathMatcher = videoIdPathPattern.matcher(data);
		if (videoIdPathMatcher.find()) {
			String videoIdPath = videoIdPathMatcher.group(1);
			if (videoIdPath != null && isValidYoutubeVideoId(videoIdPath)) {
				return videoIdPath;
			}
		}

		// could not extract video ID - throw exception
		throw new VideoIdNotFoundException();
	}

	private static boolean isValidYoutubeVideoId(String maybeVideoId) {
		return youtubeVideoIdPattern.matcher(maybeVideoId).matches();
	}

	public static class VideoIdNotFoundException extends Exception {}

}

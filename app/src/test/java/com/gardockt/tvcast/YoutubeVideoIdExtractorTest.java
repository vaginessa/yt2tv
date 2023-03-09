package com.gardockt.tvcast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import org.junit.Test;

public class YoutubeVideoIdExtractorTest {

	@Test
	public void onlyVideoId() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(data, videoId);
	}

	@Test
	public void youtubeUrl() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "https://www.youtube.com/watch?v=XWM4FrczyHU";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void youtubeUrlAllCharacterGroups() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		//given
		String data = "https://www.youtube.com/watch?v=_wC-dJ_uP94";
		String expectedVideoId = "_wC-dJ_uP94";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void youtubeUrlWithoutProtocol() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "www.youtube.com/watch?v=XWM4FrczyHU";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void youtubeUrlWithOtherParams() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "https://www.youtube.com/watch?v=XWM4FrczyHU&themeRefresh=1";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void youtubeUrlWithOtherParamsUnusualOrder() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "https://www.youtube.com/watch?themeRefresh=1&v=XWM4FrczyHU";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void youtuDotBeUrl() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "https://youtu.be/XWM4FrczyHU";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void youtubeEmbedUrl() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "https://www.youtube.com/embed/XWM4FrczyHU";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void youtubeEmbedUrlWithParams() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "https://www.youtube.com/embed/XWM4FrczyHU?autoplay=1&loop=1";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void invidiousUrl() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "https://inv.bp.projectsegfau.lt/watch?v=XWM4FrczyHU";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void invidiousEmbedUrl() throws YoutubeVideoIdExtractor.VideoIdNotFoundException {
		// given
		String data = "https://invidious.slipfox.xyz/embed/XWM4FrczyHU";
		String expectedVideoId = "XWM4FrczyHU";

		// when
		String videoId = YoutubeVideoIdExtractor.extract(data);

		// then
		assertEquals(expectedVideoId, videoId);
	}

	@Test
	public void inputWithoutVideoId() {
		assertThrows(YoutubeVideoIdExtractor.VideoIdNotFoundException.class, () -> {
			// given
			String data = "invalid";

			// when
			YoutubeVideoIdExtractor.extract(data);

			// then
			fail();
		});
	}

}

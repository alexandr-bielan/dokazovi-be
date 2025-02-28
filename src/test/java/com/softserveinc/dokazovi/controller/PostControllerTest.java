package com.softserveinc.dokazovi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.softserveinc.dokazovi.dto.post.PostDTO;
import com.softserveinc.dokazovi.dto.post.PostMainPageDTO;
import com.softserveinc.dokazovi.dto.post.PostSaveFromUserDTO;
import com.softserveinc.dokazovi.entity.enumerations.PostStatus;
import com.softserveinc.dokazovi.exception.BadRequestException;
import com.softserveinc.dokazovi.exception.EntityNotFoundException;
import com.softserveinc.dokazovi.security.UserPrincipal;
import com.softserveinc.dokazovi.service.PostService;
import com.softserveinc.dokazovi.service.PostTypeService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.util.List;
import java.util.Set;

import static com.softserveinc.dokazovi.controller.EndPoints.POST;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_ALL_POSTS;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_GET_POST_BY_AUTHOR_ID_AND_DIRECTIONS;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_IMPORTANT;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_LATEST;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_LATEST_BY_DIRECTION;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_LATEST_BY_EXPERT;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_LATEST_BY_POST_TYPES_AND_ORIGINS;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_SET_IMPORTANT;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_SET_UNIMPORTANT;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_TYPE;
import static com.softserveinc.dokazovi.controller.EndPoints.POST_VIEW_COUNT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PostControllerTest {

	private MockMvc mockMvc;

	@InjectMocks
	private PostController postController;
	@Mock
	private PostService postService;
	@Mock
	private PostTypeService postTypeService;
	@Mock
	private Validator validator;

	@BeforeEach
	public void init() {
		this.mockMvc = MockMvcBuilders
				.standaloneSetup(postController)
				.setValidator(validator)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.build();
	}

	@Test
	void getPostById_WhenExists_isOk() throws Exception {
		Integer existingPostId = 1;
		String uri = POST + "/" + existingPostId;
		PostDTO postDTO = PostDTO.builder()
				.id(existingPostId)
				.build();

		when(postService.findPostById(any(Integer.class))).thenReturn(postDTO);
		mockMvc.perform(get(uri)).andExpect(status().isOk());

		verify(postService).findPostById(eq(existingPostId));
	}

	@Test
	void getPostById_WhenNotExists_NotFound() throws Exception {
		Integer notExistingPostId = 1;
		String uri = POST + "/" + notExistingPostId;

		when(postService.findPostById(any(Integer.class))).thenReturn(null);
		mockMvc.perform(get(uri)).andExpect(status().isNotFound());

		verify(postService).findPostById(eq(notExistingPostId));
	}

	@Test
	void savePost() throws Exception {
		String content = "{\n"
				+ "  \"authorId\": 1,\n"
				+ "  \"content\": \"string\",\n"
				+ "  \"directions\": [\n"
				+ "    {\n"
				+ "      \"id\": 0\n"
				+ "    }\n"
				+ "  ],\n"
				+ "  \"origins\": [\n"
				+ "    {\n"
				+ "      \"id\": 0\n"
				+ "    }\n"
				+ "  ],\n"
				+ "  \"preview\": \"string\",\n"
				+ "  \"previewImageUrl\": \"string\",\n"
				+ "  \"title\": \"string\",\n"
				+ "  \"type\": {\n"
				+ "    \"id\": 0\n"
				+ "  },\n"
				+ "  \"videoUrl\": \"string\"\n"
				+ "}";
		ObjectMapper mapper = new ObjectMapper();
		PostSaveFromUserDTO post = mapper.readValue(content, PostSaveFromUserDTO.class);
		mockMvc.perform(post(POST)
				.contentType(MediaType.APPLICATION_JSON)
				.content(content))
				.andExpect(status().isCreated());
		verify(postService).saveFromUser(eq(post), any());
	}

	@Test
	void findLatestPublished_GetWithPagination_isOk() throws Exception {
		Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt", "id").descending());
		mockMvc.perform(get(POST + POST_LATEST + "/?page=0"))
				.andExpect(status().isOk());
		verify(postService).findAllByStatus(eq(PostStatus.PUBLISHED), eq(pageable));
	}

	@Test
	void findImportant_GetWithPagination_isOk() throws Exception {
		Pageable pageable = PageRequest.of(0, 3, Sort.by("createdAt", "id").descending());
		mockMvc.perform(get(POST + POST_IMPORTANT + "/?page=0&size=3"))
				.andExpect(status().isOk());
		verify(postService).findImportantPosts(eq(pageable));
	}

	@Test
	void findLatestByDirection() throws Exception {
		Integer directionId = 1;
		Set<Integer> type = Set.of(2);
		Set<Integer> tag = Set.of(3, 4, 5, 6);
		Pageable pageable = PageRequest.of(0, 6, Sort.by("createdAt", "id").descending());
		mockMvc.perform(
				get(POST + POST_LATEST_BY_DIRECTION + "?direction=1&page=0&size=6&type=2&tag=3,4,5,6"))
				.andExpect(status().isOk());
		verify(postService).findAllByDirection(directionId, type, tag, PostStatus.PUBLISHED, pageable);
	}

	@Test
	void getPostsByAuthorIdAndDirections_WhenExists_isOk() throws Exception {
		Set<Integer> directions = Set.of(1, 4);
		Pageable pageable = PageRequest.of(0, 12);

		String uri = POST + POST_GET_POST_BY_AUTHOR_ID_AND_DIRECTIONS + "?authorId=1&directions=1,4";
		PostDTO postDTO = PostDTO.builder()
				.id(1)
				.build();

		Page<PostDTO> page = new PageImpl<>(List.of(postDTO));

		when(postService.findPostsByAuthorIdAndDirections(any(), any(), any())).thenReturn(page);

		mockMvc.perform(get(uri)).andExpect(status().isOk());

		verify(postService).findPostsByAuthorIdAndDirections(pageable, 1, directions);
	}

	@Test
	void getPostsByAuthorIdAndDirections_WhenNotExists_NotFound() throws Exception {
		Set<Integer> directions = Set.of(1, 4);
		Pageable pageable = PageRequest.of(0, 12);

		String uri = POST + POST_GET_POST_BY_AUTHOR_ID_AND_DIRECTIONS + "?authorId=1&directions=1,4";

		Page<PostDTO> page = new PageImpl<>(List.of());

		when(postService.findPostsByAuthorIdAndDirections(any(), any(), any())).thenReturn(page);

		mockMvc.perform(get(uri)).andExpect(status().isNotFound());

		verify(postService).findPostsByAuthorIdAndDirections(pageable, 1, directions);
	}

	@Test
	void findLatestByExpert() throws Exception {
		Integer expertId = 2;
		Set<Integer> typeId = Set.of(1, 2);
		Set<Integer> directionId = Set.of(1, 2, 3);
		Pageable pageable = PageRequest.of(0, 10);
		mockMvc.perform(
				get(POST + POST_LATEST_BY_EXPERT + "?expert=2&type=1,2&direction=1,2,3"))
				.andExpect(status().isOk());
		verify(postService).findAllByExpertAndTypeAndDirections(expertId, typeId, directionId, pageable);
	}

	@Test
	void findAllPostType() throws Exception {
		mockMvc.perform(get(POST + POST_TYPE)).andExpect(status().isOk());
		verify(postTypeService).findAll();
	}

	@Test
	void archivePostById_WhenExists_isOk() throws Exception {
		String content = "{\n" +
				"  \"content\": \"string\",\n" +
				"  \"directions\": [\n" +
				"    {\n" +
				"      \"id\": 1\n" +
				"    }\n" +
				"  ],\n" +
				"  \"id\": 1,\n" +
				"  \"preview\": \"string\",\n" +
				"  \"videoUrl\": \"string\",\n" +
				"  \"previewImageUrl\": \"string\",\n" +
				"  \"tags\": [\n" +
				"    {\n" +
				"      \"id\": 1,\n" +
				"      \"tag\": \"string\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"title\": \"string\",\n" +
				"  \"type\": {\n" +
				"    \"id\": 1,\n" +
				"    \"name\": \"string\"\n" +
				"  }\n" +
				"}";

		Mockito.when(postService.archivePostById(any(UserPrincipal.class), any(Integer.class)))
				.thenReturn(true);
		mockMvc.perform(delete("/post/1").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk()).andExpect(result ->
				Assertions.assertEquals("{\"success\":true,\"message\":\"post 1 deleted successfully\"}",
						result.getResponse().getContentAsString()));
	}

	@Test
	void updatePostById_WhenExists_isOk() throws Exception {
		String content = "{\n" +
				"  \"content\": \"string\",\n" +
				"  \"directions\": [\n" +
				"    {\n" +
				"      \"id\": 1\n" +
				"    }\n" +
				"  ],\n" +
				"  \"id\": 1,\n" +
				"  \"preview\": \"string\",\n" +
				"  \"videoUrl\": \"string\",\n" +
				"  \"previewImageUrl\": \"string\",\n" +
				"  \"tags\": [\n" +
				"    {\n" +
				"      \"id\": 1,\n" +
				"      \"tag\": \"string\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"title\": \"string\",\n" +
				"  \"type\": {\n" +
				"    \"id\": 1,\n" +
				"    \"name\": \"string\"\n" +
				"  }\n" +
				"}";

		Mockito.when(postService.updatePostById(any(UserPrincipal.class), any(PostSaveFromUserDTO.class)))
				.thenReturn(true);
		mockMvc.perform(put("/post/").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk()).andExpect(result ->
				Assertions.assertEquals("{\"success\":true,\"message\":\"post 1 updated successfully\"}",
						result.getResponse().getContentAsString()));
	}

	@Test
	void archivePostById_WhenNotExists_NotFound() throws Exception {
		String content = "{\n" +
				"  \"content\": \"string\",\n" +
				"  \"directions\": [\n" +
				"    {\n" +
				"      \"id\": 1\n" +
				"    }\n" +
				"  ],\n" +
				"  \"id\": -1,\n" +
				"  \"preview\": \"string\",\n" +
				"  \"videoUrl\": \"string\",\n" +
				"  \"previewImageUrl\": \"string\",\n" +
				"  \"tags\": [\n" +
				"    {\n" +
				"      \"id\": 1,\n" +
				"      \"tag\": \"string\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"title\": \"string\",\n" +
				"  \"type\": {\n" +
				"    \"id\": 1,\n" +
				"    \"name\": \"string\"\n" +
				"  }\n" +
				"}";

		Mockito.when(postService.archivePostById(any(UserPrincipal.class), any(Integer.class)))
				.thenThrow(new EntityNotFoundException("Post with -1 not found"));

		mockMvc.perform(delete("/post/-1").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk()).andExpect(result ->
				Assertions.assertEquals("{\"success\":false,\"message\":\"Post with -1 not found\"}",
						result.getResponse().getContentAsString()));
	}

	@Test
	void updatePostById_WhenNotExists_NotFound() throws Exception {
		String content = "{\n" +
				"  \"content\": \"string\",\n" +
				"  \"directions\": [\n" +
				"    {\n" +
				"      \"id\": 1\n" +
				"    }\n" +
				"  ],\n" +
				"  \"id\": -1,\n" +
				"  \"preview\": \"string\",\n" +
				"  \"videoUrl\": \"string\",\n" +
				"  \"previewImageUrl\": \"string\",\n" +
				"  \"tags\": [\n" +
				"    {\n" +
				"      \"id\": 1,\n" +
				"      \"tag\": \"string\"\n" +
				"    }\n" +
				"  ],\n" +
				"  \"title\": \"string\",\n" +
				"  \"type\": {\n" +
				"    \"id\": 1,\n" +
				"    \"name\": \"string\"\n" +
				"  }\n" +
				"}";

		Mockito.when(postService.updatePostById(any(UserPrincipal.class), any(PostSaveFromUserDTO.class)))
				.thenThrow(new EntityNotFoundException("Post with -1 not found"));

		mockMvc.perform(put("/post/").contentType(MediaType.APPLICATION_JSON).content(content))
				.andExpect(status().isOk()).andExpect(result ->
				Assertions.assertEquals("{\"success\":false,\"message\":\"Post with -1 not found\"}",
						result.getResponse().getContentAsString()));
	}

	@Test
	void findAllPostsByDirectionsByPostTypesAndByOrigins_isOk() throws Exception {
		Set<Integer> directions = Set.of(1, 2);
		Set<Integer> types = Set.of(1, 3);
		Set<Integer> origins = Set.of(2, 3);
		Pageable pageable = PageRequest.of(0, 10);
		PostDTO postDTO = PostDTO.builder()
				.id(1)
				.build();
		Page<PostDTO> page = new PageImpl<>(List.of(postDTO));
		Mockito.when(postService.findAllByDirectionsAndByPostTypesAndByOrigins(directions, types, origins, pageable))
				.thenReturn(page);
		mockMvc.perform(get(POST + POST_ALL_POSTS + "?directions=1,2&types=1,3&origins=2,3"))
				.andExpect(status().isOk())
				.andExpect(result -> Assertions.assertEquals(1,
						getIdFromResponse(result.getResponse().getContentAsString()))
				);

		verify(postService).findAllByDirectionsAndByPostTypesAndByOrigins(directions, types, origins, pageable);
	}

	private Integer getIdFromResponse(String json) {
		try {
			JSONArray jsonArray = new JSONObject(json).getJSONArray("content");
			JSONObject obj = (JSONObject) jsonArray.get(0);
			return obj.getInt("id");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Test
	void findAllPostsByDirectionsByPostTypesAndByOrigins_CatchException() throws Exception {
		Set<Integer> directionIds = null;
		Set<Integer> typeIds = null;
		Set<Integer> originIds = null;
		Pageable pageable = PageRequest.of(0, 10);
		Page<PostDTO> page = null;

		Mockito.when(
				postService.findAllByDirectionsAndByPostTypesAndByOrigins(directionIds, typeIds, originIds, pageable))
				.thenThrow(new EntityNotFoundException(
						String.format("Fail to filter posts with params directionIds=%s, typeIds=%s, originIds=%s",
								directionIds, typeIds, originIds)))
				.thenReturn(page);

		mockMvc.perform(get(POST + POST_ALL_POSTS))
				.andExpect(status().isNoContent())
				.andExpect(result -> Assertions.assertEquals(0, result.getResponse().getContentLength()));
		verify(postService).findAllByDirectionsAndByPostTypesAndByOrigins(directionIds, typeIds, originIds, pageable);
	}

	@Test
	void findAllPostsByDirectionsByPostTypesAndByOrigins_EmptyPage() throws Exception {
		Set<Integer> directions = Set.of(-1, 1111);
		Set<Integer> types = Set.of(123, 2345);
		Set<Integer> origins = Set.of(1234, 1231);
		Pageable pageable = PageRequest.of(0, 10);
		PostDTO postDTO = PostDTO.builder()
				.id(0)
				.build();
		Page<PostDTO> page = new PageImpl<>(List.of(postDTO));

		Mockito.when(postService.findAllByDirectionsAndByPostTypesAndByOrigins(directions, types, origins, pageable))
				.thenReturn(page);
		mockMvc.perform(get(POST + POST_ALL_POSTS + "?directions=-1,1111&types=123,2345&origins=1234,1231"))
				.andExpect(status().isOk())
				.andExpect(result -> Assertions.assertEquals(0,
						getIdFromResponse(result.getResponse().getContentAsString()))
				);

		verify(postService).findAllByDirectionsAndByPostTypesAndByOrigins(directions, types, origins, pageable);
	}

	@Test
	void setPostsAsImportant() throws Exception {
		Set<Integer> postIds = Set.of(1, 2, 3, 4);
		Mockito.when(postService.setPostsAsImportant(postIds))
				.thenReturn(true);
		mockMvc.perform(get(POST + POST_SET_IMPORTANT + "?posts=1,2,3,4"))
				.andExpect(status().isOk());

		verify(postService).setPostsAsImportant(postIds);
	}

	@Test
	void setPostsAsImportant_Exception() throws Exception {
		String uri = POST + POST_SET_IMPORTANT + "?posts=";

		Mockito.when(postService.setPostsAsImportant(any()))
				.thenThrow(new BadRequestException(
						"could not execute statement; SQL [n/a]; nested "
								+ "exception is org.hibernate.exception.SQLGrammarException: "
								+ "could not execute statement"));

		mockMvc.perform(get(uri))
				.andExpect(status().isOk()).andExpect(result ->
				Assertions.assertEquals(
						"{\"success\":false,\"message\":\"could not execute statement; SQL [n/a]; nested "
								+ "exception is org.hibernate.exception.SQLGrammarException: "
								+ "could not execute statement\"}",
						result.getResponse().getContentAsString()));
	}

	@Test
	void findLatestByPostTypesAndByOrigins() throws Exception {
		PostMainPageDTO postMainPageDTO = PostMainPageDTO.builder().fieldName("Video").build();
		Page<PostMainPageDTO> page = new PageImpl<>(List.of(postMainPageDTO));
		when(postService.findLatestByPostTypesAndOrigins(any(Pageable.class))).thenReturn(page);
		mockMvc.perform(get(POST + POST_LATEST_BY_POST_TYPES_AND_ORIGINS)).andExpect(status().isOk());

		verify(postService).findLatestByPostTypesAndOrigins(any(Pageable.class));
	}

	@Test
	void setPostsAsUnimportant() throws Exception {
		Set<Integer> postIds = Set.of(1, 2, 3, 4);
		Mockito.when(postService.setPostsAsUnimportant(postIds))
				.thenReturn(true);
		mockMvc.perform(get(POST + POST_SET_UNIMPORTANT + "?posts=1,2,3,4"))
				.andExpect(status().isOk());

		verify(postService).setPostsAsUnimportant(postIds);
	}

	@Test
	void setPostsAsUnimportant_Exception() throws Exception {
		String uri = POST + POST_SET_UNIMPORTANT + "?posts=";

		Mockito.when(postService.setPostsAsUnimportant(any()))
				.thenThrow(new BadRequestException(
						"could not execute statement; SQL [n/a]; nested "
								+ "exception is org.hibernate.exception.SQLGrammarException: "
								+ "could not execute statement"));

		mockMvc.perform(get(uri))
				.andExpect(status().isOk()).andExpect(result ->
				Assertions.assertEquals(
						"{\"success\":false,\"message\":\"could not execute statement; SQL [n/a]; nested "
								+ "exception is org.hibernate.exception.SQLGrammarException: "
								+ "could not execute statement\"}",
						result.getResponse().getContentAsString()));
	}

	@Test
	void getPostViewCount() throws Exception {
		String uri = POST + POST_VIEW_COUNT + "/?url=%2Fexperts";

		mockMvc.perform(get(uri)).andExpect(status().isOk());

		verify(postService).getPostViewCount(any(String.class));
	}

}

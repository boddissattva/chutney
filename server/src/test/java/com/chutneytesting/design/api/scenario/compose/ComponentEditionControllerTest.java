package com.chutneytesting.design.api.scenario.compose;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.chutneytesting.RestExceptionHandler;
import com.chutneytesting.design.api.scenario.compose.dto.ComposableTestCaseDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableScenarioDto;
import com.chutneytesting.design.api.scenario.compose.dto.ImmutableComposableTestCaseDto;
import com.chutneytesting.design.domain.scenario.TestCaseMetadataImpl;
import com.chutneytesting.design.domain.scenario.TestCaseRepository;
import com.chutneytesting.design.domain.scenario.compose.ComposableScenario;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCase;
import com.chutneytesting.design.domain.scenario.compose.ComposableTestCaseRepository;
import com.chutneytesting.execution.domain.compiler.TestCasePreProcessors;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedScenario;
import com.chutneytesting.execution.domain.scenario.composed.ExecutableComposedTestCase;
import com.chutneytesting.security.api.UserDto;
import com.chutneytesting.security.infra.SpringUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class ComponentEditionControllerTest {

    private final String DEFAULT_COMPOSABLE_TESTCASE_DB_ID = "#30:1";
    private final String DEFAULT_COMPOSABLE_TESTCASE_ID = "30-1";

    private final ObjectMapper om = new ObjectMapper().findAndRegisterModules();
    private final ComposableTestCaseDto composableTestCaseDto =
        ImmutableComposableTestCaseDto.builder()
            .id(DEFAULT_COMPOSABLE_TESTCASE_ID)
            .title("Default title")
            .scenario(
                ImmutableComposableScenarioDto.builder().build()
            )
            .build();

    private final ComposableTestCaseRepository composableTestCaseRepository = mock(ComposableTestCaseRepository.class);
    private final TestCaseRepository testCaseRepository = mock(TestCaseRepository.class);
    private final SpringUserService userService = mock(SpringUserService.class);
    private final TestCasePreProcessors testCasePreProcessors  = mock(TestCasePreProcessors.class);
    private final UserDto currentUser = new UserDto();

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        ComponentEditionController sut = new ComponentEditionController(composableTestCaseRepository, testCaseRepository, userService, testCasePreProcessors);

        mockMvc = MockMvcBuilders.standaloneSetup(sut)
            .setControllerAdvice(new RestExceptionHandler())
            .build();

        when(composableTestCaseRepository.findById(any()))
            .thenReturn(new ComposableTestCase(DEFAULT_COMPOSABLE_TESTCASE_DB_ID, TestCaseMetadataImpl.builder().build(), ComposableScenario.builder().build()));

        when(composableTestCaseRepository.save(any()))
            .thenReturn(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);

        currentUser.setId("currentUser");
        when(userService.currentUser()).thenReturn(currentUser);
    }

    @Test
    public void should_save_testCase() throws Exception {
        // When
        mockMvc.perform(post(ComponentEditionController.BASE_URL)
            .contentType(APPLICATION_JSON_VALUE)
            .content(om.writeValueAsString(composableTestCaseDto)))
            .andExpect(status().isOk());

        // Then
        verify(composableTestCaseRepository).save(any());
    }

    @Test
    public void should_find_testCase() throws Exception {
        // When
        mockMvc.perform(get(ComponentEditionController.BASE_URL + "/" + DEFAULT_COMPOSABLE_TESTCASE_ID));

        // Then
        verify(composableTestCaseRepository).findById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
    }

    @Test
    public void should_find_testCase_with_parameters_replaced() throws Exception {
        // Given
        when(testCasePreProcessors.apply(any()))
            .thenReturn(new ExecutableComposedTestCase(
            TestCaseMetadataImpl.builder()
                .build(),
            ExecutableComposedScenario.builder().build()
        ));

        // When
        mockMvc.perform(get(ComponentEditionController.BASE_URL + "/" + DEFAULT_COMPOSABLE_TESTCASE_ID + "/executable" ));

        // Then
        verify(testCaseRepository).findById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
        verify(testCasePreProcessors).apply(any());
    }

    @Test
    public void should_delete_testCase() throws Exception {
        // When
        mockMvc.perform(delete(ComponentEditionController.BASE_URL + "/" + DEFAULT_COMPOSABLE_TESTCASE_ID));

        // Then
        verify(composableTestCaseRepository).removeById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
        verify(testCaseRepository).removeById(DEFAULT_COMPOSABLE_TESTCASE_DB_ID);
    }
}

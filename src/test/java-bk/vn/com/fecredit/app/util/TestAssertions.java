package vn.com.fecredit.app.util;

import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import vn.com.fecredit.app.dto.RewardDTO;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class TestAssertions {

    public static void assertRewardResponse(ResultActions result, RewardDTO.Response expected) throws Exception {
        result.andDo(MockMvcResultHandlers.print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.id").value(expected.getId()))
              .andExpect(jsonPath("$.name").value(expected.getName()))
              .andExpect(jsonPath("$.quantity").value(expected.getQuantity()))
              .andExpect(jsonPath("$.remainingQuantity").value(expected.getRemainingQuantity()))
              .andExpect(jsonPath("$.active").value(expected.isActive()));
    }

    public static void assertRewardCreated(ResultActions result, RewardDTO.Response expected) throws Exception {
        result.andDo(MockMvcResultHandlers.print())
              .andExpect(status().isCreated())
              .andExpect(jsonPath("$.id").exists())
              .andExpect(jsonPath("$.name").value(expected.getName()))
              .andExpect(jsonPath("$.quantity").value(expected.getQuantity()))
              .andExpect(jsonPath("$.remainingQuantity").value(expected.getQuantity()));
    }

    public static void assertBadRequest(ResultActions result, String message) throws Exception {
        result.andDo(MockMvcResultHandlers.print())
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.message", containsString(message)));
    }

    public static void assertNotFound(ResultActions result, String message) throws Exception {
        result.andDo(MockMvcResultHandlers.print())
              .andExpect(status().isNotFound())
              .andExpect(jsonPath("$.message", containsString(message)));
    }

    public static void assertNoContent(ResultActions result) throws Exception {
        result.andDo(MockMvcResultHandlers.print())
              .andExpect(status().isNoContent());
    }

    public static void assertPagination(ResultActions result, int totalElements, int totalPages) throws Exception {
        result.andDo(MockMvcResultHandlers.print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.totalElements").value(totalElements))
              .andExpect(jsonPath("$.totalPages").value(totalPages))
              .andExpect(jsonPath("$.content").isArray());
    }

    public static void assertEntityStatus(ResultActions result, EntityStatus status) throws Exception {
        result.andDo(MockMvcResultHandlers.print())
              .andExpect(status().isOk())
              .andExpect(jsonPath("$.status").value(status.name()));
    }

    public static void assertValidationError(ResultActions result, String field, String message) throws Exception {
        result.andDo(MockMvcResultHandlers.print())
              .andExpect(status().isBadRequest())
              .andExpect(jsonPath("$.errors[?(@.field == '" + field + "')].message", 
                  hasItem(containsString(message))));
    }
}

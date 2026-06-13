package com.entri.modules.service;

import com.entri.common.exception.NotFoundException;
import com.entri.modules.events.dto.CategoryResponse;
import com.entri.modules.events.entity.EventCategory;
import com.entri.modules.events.repository.EventCategoryRepository;
import com.entri.modules.events.service.EventCategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventCategoryServiceTest {

    @Mock EventCategoryRepository eventCategoryRepository;

    @InjectMocks EventCategoryService eventCategoryService;

    // ── getAllCategories ───────────────────────────────────────────────

    @Test
    void getAllCategories_noCategoriesExist_returnsEmptyList() {
        when(eventCategoryRepository.findAll()).thenReturn(List.of());

        assertThat(eventCategoryService.getAllCategories()).isEmpty();
    }

    @Test
    void getAllCategories_categoriesExist_returnsMappedResponses() {
        when(eventCategoryRepository.findAll()).thenReturn(List.of(
                category(1L, "Music & Concerts"),
                category(2L, "Sports & Fitness")
        ));

        List<CategoryResponse> result = eventCategoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Music & Concerts");
        assertThat(result.get(1).id()).isEqualTo(2L);
        assertThat(result.get(1).name()).isEqualTo("Sports & Fitness");
    }

    @Test
    void getAllCategories_mapsIdCorrectly() {
        when(eventCategoryRepository.findAll()).thenReturn(List.of(category(42L, "Technology")));

        assertThat(eventCategoryService.getAllCategories().get(0).id()).isEqualTo(42L);
    }

    @Test
    void getAllCategories_mapsNameCorrectly() {
        when(eventCategoryRepository.findAll()).thenReturn(List.of(category(1L, "Arts & Culture")));

        assertThat(eventCategoryService.getAllCategories().get(0).name()).isEqualTo("Arts & Culture");
    }

    @Test
    void getAllCategories_returnsAllCategories() {
        List<EventCategory> categories = List.of(
                category(1L, "Music & Concerts"),
                category(2L, "Sports & Fitness"),
                category(3L, "Food & Drink"),
                category(4L, "Technology")
        );
        when(eventCategoryRepository.findAll()).thenReturn(categories);

        assertThat(eventCategoryService.getAllCategories()).hasSize(4);
    }

    @Test
    void getAllCategories_callsFindAll() {
        when(eventCategoryRepository.findAll()).thenReturn(List.of());

        eventCategoryService.getAllCategories();

        verify(eventCategoryRepository).findAll();
    }

    // ── findById ──────────────────────────────────────────────────────

    @Test
    void findById_categoryExists_returnsCategory() {
        EventCategory category = category(1L, "Music & Concerts");
        when(eventCategoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThat(eventCategoryService.findById(1L)).isEqualTo(category);
    }

    @Test
    void findById_categoryNotFound_throwsNotFoundException() {
        when(eventCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventCategoryService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    void findById_callsRepositoryWithCorrectId() {
        EventCategory category = category(5L, "Technology");
        when(eventCategoryRepository.findById(5L)).thenReturn(Optional.of(category));

        eventCategoryService.findById(5L);

        verify(eventCategoryRepository).findById(5L);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private EventCategory category(Long id, String name) {
        return EventCategory.builder().id(id).name(name).description("").build();
    }
}

package com.entri.modules.events.service;

import com.entri.events.dto.CategoryRequest;
import com.entri.events.entity.EventCategory;
import com.entri.events.repository.EventCategoryRepository;
import com.entri.events.service.EventCategoryService;
import com.entri.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private EventCategory buildCategory(Long id, String name, String description) {
        return EventCategory.builder()
                .id(id)
                .name(name)
                .description(description)
                .build();
    }

    @Test
    void getAllCategories_returnsMappedResponses() {
        var category1 = buildCategory(1L, "Music", "Music events");
        var category2 = buildCategory(2L, "Sports", "Sports events");
        when(eventCategoryRepository.findAll()).thenReturn(List.of(category1, category2));

        var result = eventCategoryService.getAllCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).name()).isEqualTo("Music");
        assertThat(result.get(1).name()).isEqualTo("Sports");
    }

    @Test
    void getAllCategories_noCategories_returnsEmptyList() {
        when(eventCategoryRepository.findAll()).thenReturn(List.of());

        var result = eventCategoryService.getAllCategories();

        assertThat(result).isEmpty();
    }

    @Test
    void createCategory_savesAndReturnsResponse() {
        var request = new CategoryRequest("Music", "Music events");
        var saved = buildCategory(1L, "Music", "Music events");
        when(eventCategoryRepository.save(org.mockito.ArgumentMatchers.any(EventCategory.class))).thenReturn(saved);

        var result = eventCategoryService.createCategory(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Music");
        assertThat(result.description()).isEqualTo("Music events");
    }

    @Test
    void createCategory_passesRequestFieldsToSavedEntity() {
        var request = new CategoryRequest("Theatre", "Theatre events");
        when(eventCategoryRepository.save(org.mockito.ArgumentMatchers.any(EventCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var captor = ArgumentCaptor.forClass(EventCategory.class);

        eventCategoryService.createCategory(request);

        verify(eventCategoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Theatre");
        assertThat(captor.getValue().getDescription()).isEqualTo("Theatre events");
    }

    @Test
    void updateCategory_categoryNotFound_throwsResourceNotFoundException() {
        when(eventCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        var request = new CategoryRequest("Music", "Music events");

        assertThatThrownBy(() -> eventCategoryService.updateCategory(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCategory_existingCategory_updatesFieldsAndReturnsResponse() {
        var existing = buildCategory(1L, "Old Name", "Old description");
        when(eventCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(eventCategoryRepository.save(existing)).thenReturn(existing);

        var request = new CategoryRequest("New Name", "New description");

        var result = eventCategoryService.updateCategory(1L, request);

        assertThat(result.name()).isEqualTo("New Name");
        assertThat(result.description()).isEqualTo("New description");
        assertThat(existing.getName()).isEqualTo("New Name");
        assertThat(existing.getDescription()).isEqualTo("New description");
    }

    @Test
    void deleteCategory_categoryNotFound_throwsResourceNotFoundException() {
        when(eventCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventCategoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCategory_existingCategory_deletesIt() {
        var existing = buildCategory(1L, "Music", "Music events");
        when(eventCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        eventCategoryService.deleteCategory(1L);

        verify(eventCategoryRepository).delete(existing);
    }

    @Test
    void findById_categoryNotFound_throwsResourceNotFoundException() {
        when(eventCategoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventCategoryService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found");
    }

    @Test
    void findById_existingCategory_returnsEntity() {
        var existing = buildCategory(1L, "Music", "Music events");
        when(eventCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));

        var result = eventCategoryService.findById(1L);

        assertThat(result).isEqualTo(existing);
    }
}

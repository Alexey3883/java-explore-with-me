package ru.practicum.compilation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.dto.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequiredArgsConstructor
public class CompilationController {

    public final CompilationService compilationService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/compilations")
    public CompilationDto addCompilationAdmin(@Valid @RequestBody NewCompilationDto newCompilationDto) {
        log.info("Получен запрос на добавление новой компиляции {}", newCompilationDto);
        return compilationService.addCompilationAdmin(newCompilationDto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/compilations/{compId}")
    public void deleteCompilationAdmin(@PathVariable Long compId) {
        compilationService.deleteCompilationAdmin(compId);
    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/admin/compilations/{compId}")
    public CompilationDto updateCompilationAdmin(@PathVariable Long compId,
                                                 @Valid @RequestBody UpdateCompilationRequest updateCompilationRequest
    ) {
        return compilationService.updateCompilationAdmin(compId, updateCompilationRequest);
    }

    @GetMapping("/compilations")
    @ResponseStatus(HttpStatus.OK)
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(required = false, defaultValue = "0") Integer from,
                                                @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("Получение списка подборок");
        return compilationService.getCompilationsPublic(pinned, PageRequest.of(from, size));
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        return compilationService.getCompilationPublic(compId);
    }

}

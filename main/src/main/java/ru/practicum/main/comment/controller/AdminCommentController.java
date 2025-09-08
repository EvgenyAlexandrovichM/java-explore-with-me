package ru.practicum.main.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.main.comment.service.AdminCommentService;

@RestController
@RequestMapping()
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {

    private final AdminCommentService service;
}

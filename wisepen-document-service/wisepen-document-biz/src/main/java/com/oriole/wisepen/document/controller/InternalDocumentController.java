package com.oriole.wisepen.document.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.document.api.feign.RemoteDocumentService;
import com.oriole.wisepen.document.service.IDocumentService;
import com.oriole.wisepen.resource.domain.dto.req.ResourceForkRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@Tag(name = "内部 - 文档", description = "供业务微服务调用的文档内部接口")
@RestController
@RequestMapping("/internal/document")
@RequiredArgsConstructor
public class InternalDocumentController implements RemoteDocumentService {

    private final IDocumentService documentService;

    @Override
    @PostMapping("/forkDocument")
    public R<Void> forkDocument(@Valid @RequestBody ResourceForkRequest request) {
        documentService.forkDocument(request);
        return R.ok();
    }
}

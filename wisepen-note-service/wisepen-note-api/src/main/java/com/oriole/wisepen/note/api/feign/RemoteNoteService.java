package com.oriole.wisepen.note.api.feign;

import com.oriole.wisepen.common.core.domain.R;
import com.oriole.wisepen.note.api.domain.dto.res.NoteSnapshotResponse;
import com.oriole.wisepen.resource.domain.dto.req.ResourceForkRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(contextId = "remoteNoteService", value = "wisepen-note-service")
public interface RemoteNoteService {

    @GetMapping("/internal/note/getNoteLatestVersion")
    R<NoteSnapshotResponse> getNoteLatestVersion(@RequestParam("resourceId") String resourceId);

    @GetMapping("/internal/note/getNoteSnapshot")
    R<NoteSnapshotResponse> getNoteSnapshot(@RequestParam("resourceId") String resourceId,
                                            @RequestParam("version") Long version);

    @PostMapping("/internal/note/forkNote")
    R<Void> forkNote(@RequestBody ResourceForkRequest request);
}

package com.oriole.wisepen.note.service;

import com.oriole.wisepen.note.api.domain.base.NoteInfoBase;
import com.oriole.wisepen.note.api.domain.dto.req.NoteCreateRequest;
import com.oriole.wisepen.note.api.domain.dto.req.NoteForkRequest;

import java.util.List;

public interface INoteService {

    String createNote(NoteCreateRequest request, String userId);

    void deleteNotes(List<String> resourceIds);

    NoteInfoBase getNoteInfo(String resourceId);

    String forkNote(NoteForkRequest request, String forkedResourceOwnerId);
}

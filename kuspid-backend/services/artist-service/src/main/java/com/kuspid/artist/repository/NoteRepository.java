package com.kuspid.artist.repository;

import com.kuspid.artist.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
}

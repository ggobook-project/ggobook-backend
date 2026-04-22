package com.untitled.ggobook.repository;

import com.untitled.ggobook.domain.ComicToon;
import com.untitled.ggobook.domain.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComicToonRepository extends JpaRepository<ComicToon, Long> {


    List<ComicToon> findByEpisode(Episode savedEpisode);
}

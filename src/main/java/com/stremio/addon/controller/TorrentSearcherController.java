package com.stremio.addon.controller;

import com.stremio.addon.controller.dto.TorrentSearcher;
import com.stremio.addon.service.TorrentSearcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/searchers", produces = "application/json")
public class TorrentSearcherController {

    private final TorrentSearcherService searcherService;

    @Autowired
    public TorrentSearcherController(TorrentSearcherService searcherService) {
        this.searcherService = searcherService;
    }

    // Obtener todos los buscadores
    @GetMapping
    public ResponseEntity<List<TorrentSearcher>> getSearchers() {
        List<TorrentSearcher> searchers = searcherService.getAllSearchers();
        return new ResponseEntity<>(searchers, HttpStatus.OK);
    }

    // Obtener un buscador por ID
    @GetMapping("/{id}")
    public ResponseEntity<TorrentSearcher> getSearcherById(@PathVariable Long id) {
        return searcherService.getSearcherById(id)
                .map(searcher -> new ResponseEntity<>(searcher, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Guardar un nuevo buscador o actualizar uno existente
    @PostMapping("/save")
    public ResponseEntity<TorrentSearcher> saveSearcher(@RequestBody TorrentSearcher searcher) {
        TorrentSearcher savedSearcher = searcherService.saveSearcher(searcher);
        return new ResponseEntity<>(savedSearcher, HttpStatus.CREATED);
    }

    // Eliminar un buscador por ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteSearcher(@PathVariable Long id) {
        if (searcherService.getSearcherById(id).isPresent()) {
            searcherService.deleteSearcher(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/")
    public String index() {
        return "index";

    }
}

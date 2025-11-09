package com.ditto.example.khitto.web;

import com.ditto.example.khitto.model.*;
import com.ditto.example.khitto.repo.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class MenuController {

    private final GameCsvRepository gameRepo;

    public MenuController(GameCsvRepository gameRepo) {
        this.gameRepo = gameRepo;
    }

    @GetMapping("/")
    public String menu(Model model) {
        model.addAttribute("games", gameRepo.findAll());
        return "menu";
    }

    @PostMapping("/games/new")
    public String newGame() {
        int id = gameRepo.getNextId();
        Game g = new Game(id, "", 0, false);
        gameRepo.save(g);
        return "redirect:/maker/" + id;
    }
}

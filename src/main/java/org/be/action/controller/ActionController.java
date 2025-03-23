package org.be.action.controller;

import org.be.action.model.Action;
import org.be.action.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    @Autowired
    private ActionService actionService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Action>> getUserActions(@PathVariable Long userId) {
        return ResponseEntity.ok(actionService.getUserActions(userId));
    }
}

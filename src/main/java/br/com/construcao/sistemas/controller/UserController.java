package br.com.construcao.sistemas.controller;

import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.model.User;
import br.com.construcao.sistemas.service.SuspectService;
import br.com.construcao.sistemas.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


    private final UserService userService;


    private final SuspectService suspectService;

    public UserController(UserService userService, SuspectService suspectService) {
        this.userService = userService;
        this.suspectService = suspectService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById( @PathVariable Long id){
        return new ResponseEntity<>(this.userService.findUserById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<User> createUser( @RequestBody  User user){
        return new ResponseEntity<>(this.userService.createUser(user), HttpStatus.CREATED);
    }


    @PostMapping("/create-suspect")
    public ResponseEntity<Suspect> createSuspect(@RequestBody Suspect suspect){
      return new ResponseEntity<>(  this.suspectService.createdSuspect(suspect), HttpStatus.CREATED);
    }


    @GetMapping("/suspects")
    public ResponseEntity<List<Suspect>> findAllSuspect(){
        return new ResponseEntity<>(  this.suspectService.findAll(), HttpStatus.OK);
    }

    @GetMapping("/suspect/{id}")
    public ResponseEntity<Suspect>  findByIdSuspect(@PathVariable  Long id){
        return new ResponseEntity<>(  this.suspectService.findById(id), HttpStatus.CREATED);
    }






}

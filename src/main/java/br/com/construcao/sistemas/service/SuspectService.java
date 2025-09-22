package br.com.construcao.sistemas.service;


import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.repository.SuspectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuspectService {

    private final SuspectRepository suspectRepository;

    public SuspectService(SuspectRepository suspectRepository) {
        this.suspectRepository = suspectRepository;
    }

    public Suspect findById(Long id){
        return suspectRepository.findById(id).get();
    }


    public Suspect createdSuspect(Suspect suspect){
        return suspectRepository.save(suspect);
    }


    public List<Suspect> findAll(){
        return suspectRepository.findAll();
    }
}

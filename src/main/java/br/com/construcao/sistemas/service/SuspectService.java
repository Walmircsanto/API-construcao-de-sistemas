package br.com.construcao.sistemas.service;


import br.com.construcao.sistemas.controller.dto.mapper.MyModelMapper;
import br.com.construcao.sistemas.controller.dto.request.suspect.CreateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.request.suspect.UpdateSuspectRequest;
import br.com.construcao.sistemas.controller.dto.response.suspect.SuspectResponse;
import br.com.construcao.sistemas.controller.exceptions.ConflictException;
import br.com.construcao.sistemas.controller.exceptions.NotFoundException;
import br.com.construcao.sistemas.model.Suspect;
import br.com.construcao.sistemas.repository.SuspectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SuspectService {

    private final SuspectRepository suspectRepository;
    private final MyModelMapper mapper;

    public SuspectService(SuspectRepository suspectRepository, MyModelMapper mapper) {
        this.suspectRepository = suspectRepository;
        this.mapper = mapper;
    }

    public SuspectResponse create(CreateSuspectRequest req){
        if (suspectRepository.existsByCpf(req.getCpf())) throw new ConflictException("CPF já cadastrado");

        Suspect s = mapper.mapTo(req, Suspect.class);
        return mapper.mapTo(suspectRepository.save(s), SuspectResponse.class);
    }

    public SuspectResponse get(Long id){
        Suspect s = suspectRepository.findById(id).orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));
        return mapper.mapTo(s, SuspectResponse.class);
    }

    public Page<SuspectResponse> list(Pageable pageable){
        return suspectRepository.findAll(pageable).map(x -> mapper.mapTo(x, SuspectResponse.class));
    }

    public SuspectResponse update(Long id, UpdateSuspectRequest req){
        Suspect s = suspectRepository.findById(id).orElseThrow(() -> new NotFoundException("Suspeito não encontrado"));

        if (req.getName()!=null) s.setName(req.getName());
        if (req.getAge()!=null) s.setAge(req.getAge());
        if (req.getUrlImage()!=null) s.setUrlImage(req.getUrlImage());
        if (req.getDescription()!=null) s.setDescription(req.getDescription());

        if (req.getCpf()!=null && !req.getCpf().equals(s.getCpf())) {
            if (suspectRepository.existsByCpf(req.getCpf())) throw new ConflictException("CPF já cadastrado");
            s.setCpf(req.getCpf());
        }

        return mapper.mapTo(suspectRepository.save(s), SuspectResponse.class);
    }

    public void delete(Long id){
        if (!suspectRepository.existsById(id)) throw new NotFoundException("Suspeito não encontrado");
        suspectRepository.deleteById(id);
    }
}

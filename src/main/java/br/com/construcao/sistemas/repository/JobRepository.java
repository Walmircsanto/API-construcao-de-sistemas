package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Optional<Job> findByJobIdRequest(String jobIdRequest);
}
package br.com.construcao.sistemas.repository;

import br.com.construcao.sistemas.model.SearchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SearchResultRepository extends JpaRepository<SearchResult, Long> {
    Optional<SearchResult> findByRequestId(String requestId);
}
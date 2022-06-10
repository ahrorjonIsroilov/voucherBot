package ent.service;

import ent.entity.Transaction;
import ent.repo.auth.TransactionRepo;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService implements BaseService {
    private final TransactionRepo repo;

    public TransactionService(TransactionRepo repo) {
        this.repo = repo;
    }

    public void save(Transaction t) {
        repo.save(t);
    }

    public List<Transaction> getAll(Date date) {
        return repo.getAllByTransactionTimeIsAfter(date);
    }

    public Optional<Transaction> findById(Long id) {
        return repo.findById(id);
    }
}

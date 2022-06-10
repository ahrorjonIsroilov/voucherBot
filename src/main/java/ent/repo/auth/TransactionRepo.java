package ent.repo.auth;

import ent.entity.Transaction;
import ent.repo.BaseRepo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface TransactionRepo extends BaseRepo, CrudRepository<Transaction, Long> {

    List<Transaction> getAllByTransactionTimeIsAfter(Date date);
}

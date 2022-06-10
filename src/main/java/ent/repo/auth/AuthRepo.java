package ent.repo.auth;

import ent.entity.auth.AuthUser;
import ent.repo.BaseRepo;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface AuthRepo extends BaseRepo, CrudRepository<AuthUser, Long> {

    AuthUser findByChatId(Long chatId);

    List<AuthUser> findAllByRoleAndRegisteredTrue(String role, Pageable pageable);

    List<AuthUser> findAllByRoleAndRegisteredTrue(String role);

    AuthUser findByUsername(String username);

    AuthUser findById(String id);

    @Transactional
    @Modifying
    @Query(value = "update AuthUser set registered = true where chatId =:chatId")
    void registerUser(@Param("chatId") Long chatId);

    @Transactional
    @Modifying
    @Query(value = "update AuthUser  set registered=:registered,balance=:balance,balanceLimit=:balanceLimit,role=:role where username=:username")
    void registerUser(
            @Param("registered") Boolean registered,
            @Param("balance") Long balance,
            @Param("balanceLimit") Long balanceLimit,
            @Param("role") String role,
            @Param("username") String username);

    @Transactional
    @Modifying
    @Query(value = "update AuthUser set registered=:registered,name=:name,chatId=:chatId,state=:state,blocked=:blocked,page=:page where username=:username")
    void registerUser(
            @Param("username") String username,
            @Param("registered") Boolean registered,
            @Param("name") String name,
            @Param("chatId") Long chatId,
            @Param("state") String state,
            @Param("blocked") Boolean blocked,
            @Param("page") Integer page);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update public.auth_user a set page = ? where a.chat_id = ?")
    void updatePage(Integer page, Long chatId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "update public.auth_user a set balance_limit = ? where a.id = ?")
    void setUserBalanceLimit(Long limit, String userId);

    @Transactional
    @Modifying
    @Query("update AuthUser set role=:role,registered=:registered where id=:userId")
    void assignAdmin(
            @Param("role") String role,
            @Param("registered") Boolean registered,
            @Param("userId") String id);

    @Transactional
    @Modifying
    @Query("update AuthUser set blocked=:status where id=:id")
    void setUserBlockedStatus(
            @Param("id") String id,
            @Param("status") Boolean status);

    @Transactional
    @Modifying
    @Query("update AuthUser set balance=:amount where id =:id")
    void withdrawMoney(Long amount, String id);

    @Transactional
    @Modifying
    @Query("delete from AuthUser where id=:userId")
    void removeAdminById(@Param("userId") String id);

    @Transactional
    @Modifying
    @Query(value = "update public.auth_user a set balance=a.balance_limit where (a.registered=true and a.blocked=false and a.role='user')", nativeQuery = true)
    void restoreVoucher();
}

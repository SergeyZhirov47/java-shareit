package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByIdAndOwnerId(long id, long ownerId);

    List<Item> findByOwnerId(long ownerId);

    List<Item> findByOwnerId(long ownerId, Pageable pageable);

    boolean existsByIdAndOwnerId(long itemId, long userId);

    @Query("SELECT it FROM Item as it WHERE it.isAvailable = true AND (lower(it.name) LIKE lower(concat('%', :searchText,'%')) OR lower(it.description) LIKE lower(concat('%', :searchText,'%')))")
    List<Item> findAvailableByNameOrDescription(@Param("searchText") String text);

    @Query("SELECT it FROM Item as it WHERE it.isAvailable = true AND (lower(it.name) LIKE lower(concat('%', :searchText,'%')) OR lower(it.description) LIKE lower(concat('%', :searchText,'%')))")
    List<Item> findAvailableByNameOrDescription(@Param("searchText") String text, Pageable pageable);

    @Query("SELECT it FROM Item as it WHERE it.request.id = :requestId")
    List<Item> findItemsForItemRequest(@Param("requestId") long requestId);

    @Query("SELECT it FROM Item as it WHERE it.request.id IN (:requestIds)")
    List<Item> findItemsForItemRequests(@Param("requestIds") List<Long> requestIds);
}

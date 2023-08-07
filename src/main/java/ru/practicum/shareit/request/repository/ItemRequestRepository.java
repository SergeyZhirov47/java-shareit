package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestorId(long requestorId);

    List<ItemRequest> findByRequestorId(long requestorId, Sort sort);

    List<ItemRequest> findByRequestorIdNot(long requestorId, Sort sort);

    List<ItemRequest> findByRequestorIdNot(long requestorId, Pageable pageable);
}

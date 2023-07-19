package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking>, CustomBookingRepository {
    @Query("SELECT case when count(b)> 0 then true else false end FROM Booking b WHERE b.id = :bookingId AND b.item.owner.id = :userId")
    boolean isUserOwnItemFromBooking(@Param("bookingId") long bookingId, @Param("userId") long userId);

    @Query("SELECT case when count(b)> 0 then true else false end FROM Booking b WHERE b.id = :bookingId AND b.booker.id = :userId")
    boolean isUserBookingAuthor(@Param("bookingId") long bookingId, @Param("userId") long userId);

    /*
    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status =ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.end <= :endDate ORDER BY b.end DESC LIMIT 1")
    Booking getLastBookingForItemById(@Param("itemId") long itemId, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.start >= :startDate ORDER BY b.start LIMIT 1")
    Booking getNextBookingForItemById(@Param("itemId") long itemId, @Param("startDate") LocalDateTime startDate);
     */
}

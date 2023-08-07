package ru.practicum.shareit.booking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking>, CustomBookingRepository {
    @Query("SELECT case when count(b)> 0 then true else false end FROM Booking b WHERE b.id = :bookingId AND b.item.owner.id = :userId")
    boolean isUserOwnItemFromBooking(@Param("bookingId") long bookingId, @Param("userId") long userId);

    @Query("SELECT case when count(b)> 0 then true else false end FROM Booking b WHERE b.id = :bookingId AND b.booker.id = :userId")
    boolean isUserBookingAuthor(@Param("bookingId") long bookingId, @Param("userId") long userId);
}

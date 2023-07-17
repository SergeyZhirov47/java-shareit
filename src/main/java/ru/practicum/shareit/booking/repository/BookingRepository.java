package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT case when count(b)> 0 then true else false end FROM Booking b WHERE b.id = :bookingId AND b.item.owner.id = :userId")
    boolean isUserOwnItemFromBooking(@Param("bookingId") long bookingId, @Param("userId") long userId);
}

package ru.practicum.shareit.booking.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.nonNull;
import static ru.practicum.shareit.booking.model.QBooking.booking;

@Slf4j
public class CustomBookingRepositoryImpl implements CustomBookingRepository {
    private final JPAQueryFactory queryFactory;
    @PersistenceContext
    private EntityManager entityManager;

    public CustomBookingRepositoryImpl(EntityManager entityManager) {
        queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
    }

    @Override
    public Booking getLastBookingForItemById(long itemId, LocalDateTime endDate) {
        final BooleanExpression whereExp = getForItem(itemId).and(getLastBookingApproved(endDate));
        final JPAQuery<Booking> query = getLastBookingSelectQuery(whereExp);

        return query.fetchFirst();
    }

    @Override
    public Map<Long, Booking> getLastBookingForItemsByIdList(List<Long> itemIdList, LocalDateTime endDate) {
        final BooleanExpression whereExp = getForItemList(itemIdList).and(getLastBookingApproved(endDate));

        // ToDo !!!
        // Когда беру по in (itemIds), то нужно брать только одну запись.
        // 1. Нужно тогда при сборе в мапу отсекать повторы (по идее можно брать первые значения для каждого booking).
        // минус - коряво и в запросе получаю больше данных, чем нужно.
        // 2. По идее нужно делать подзапрос для каждой вещи (чтобы дата равнялась мин или макс для этой вещи)
        // минус - сложно. зато получаю ровно те данные, что нужно

// ToDo
        // Даже не понимаю как запрос составить
//        select b1.* from Bookings b1
//        inner join (
//                select  b2.itemId, max(b2.end) as max_value from Bookings b2 -- нужно b2.id или нет?
//              WHERE b2.status = Approved AND b2.end > :value
//                  group by  b2.itemId
//) ON b1.itemId = b2.itemId -- AND b1.end = b2.max_value ???
        // WHERE b1.status = Approved AND b1.itemId in (...)  b2.end > :value
//        and b1.end = max_value -- или сработает в ON и эта часть не нужна

        final JPAQuery<Booking> query = getLastBookingSelectQuery(whereExp);//.groupBy(booking.id, booking.item.id);

        final List<Booking> lastBookings = query.fetch();
        log.info("#lastBookings count = " + lastBookings.size());

        final Map<Long, Booking> hackMap = new HashMap<>();
        for (Booking b : lastBookings) {
            final long itemId = b.getItem().getId();

            if (!hackMap.containsKey(itemId)) {
                hackMap.put(itemId, b);
            }
        }

        return hackMap;
        // return lastBookings.stream().collect(Collectors.toUnmodifiableMap(b -> b.getItem().getId(), b -> b));
    }

    @Override
    public Booking getNextBookingForItemById(long itemId, LocalDateTime startDate) {
        final BooleanExpression whereExp = getForItem(itemId).and(getNextBookingApproved(startDate));
        final JPAQuery<Booking> query = getNextBookingSelectQuery(whereExp);

        return query.fetchFirst();
    }

    @Override
    public Map<Long, Booking> getNextBookingForItemsByIdList(List<Long> itemIdList, LocalDateTime startDate) {
        final BooleanExpression whereExp = getForItemList(itemIdList).and(getNextBookingApproved(startDate));
        final JPAQuery<Booking> query = getNextBookingSelectQuery(whereExp);//.groupBy(booking.id, booking.item.id);

        final List<Booking> nextBookings = query.fetch();
        log.info("#nextBookings count = " + nextBookings.size());

        final Map<Long, Booking> hackMap = new HashMap<>();
        for (Booking b : nextBookings) {
            final long itemId = b.getItem().getId();

            if (!hackMap.containsKey(itemId)) {
                hackMap.put(itemId, b);
            }
        }

        return hackMap;

        //return nextBookings.stream().collect(Collectors.toUnmodifiableMap(b -> b.getItem().getId(), b -> b));
    }

    @Override
    public boolean isUserBookingItem(long userId, long itemId, LocalDateTime startUsingBeforeDate) {
        BooleanExpression isUserBookerExp = booking.booker.id.eq(userId);
        BooleanExpression itemExp = booking.item.id.eq(itemId);
        BooleanExpression statusExp = booking.status.eq(BookingStatus.APPROVED);
        BooleanExpression bookingPeriodExp = booking.start.before(startUsingBeforeDate); // неважно закончилась аренда или нет. главное, что уже начал пользоваться.
        BooleanExpression whereExp = isUserBookerExp.and(itemExp).and(statusExp).and(bookingPeriodExp);

        // ToDo
        // По идее должен быть способ чуть лучше. Мне весь букинг не нужен. только наличие такой строки

        // так возможно
       /*
        queryFactory.select(  queryFactory.selectFrom(booking)
                .where(whereExp)
                .exists()).fetchFirst()
         */

        final Booking oneOfBooking = queryFactory.selectFrom(booking)
                .where(whereExp)
                .fetchFirst();

        return nonNull(oneOfBooking);
    }

    private BooleanExpression getForItem(long itemId) {
        return booking.item.id.eq(itemId);
    }

    private BooleanExpression getForItemList(List<Long> itemIds) {
        return booking.item.id.in(itemIds);
    }

    private BooleanExpression getWithBookingStatus(BookingStatus status) {
        return booking.status.eq(status);
    }

    private BooleanExpression getApproved() {
        return getWithBookingStatus(BookingStatus.APPROVED);
    }

    private BooleanExpression getLastBooking(LocalDateTime endDate) {
        return booking.start.before(endDate);
    }

    private BooleanExpression getNextBooking(LocalDateTime startDate) {
        return booking.start.after(startDate);
    }

    private BooleanExpression getLastBookingApproved(LocalDateTime endDate) {
        return getApproved().and(getLastBooking(endDate));
    }

    private BooleanExpression getNextBookingApproved(LocalDateTime startDate) {
        return getApproved().and(getNextBooking(startDate));
    }

    private JPAQuery<Booking> getBookingSelectQuery(BooleanExpression whereExp, OrderSpecifier<LocalDateTime> orderByDate) {
        return queryFactory.selectFrom(booking)
                .where(whereExp)
                .orderBy(orderByDate);
    }

    private JPAQuery<Booking> getLastBookingSelectQuery(BooleanExpression whereExp) {
        var orderBy = booking.end.desc();
        return getBookingSelectQuery(whereExp, orderBy);
    }

    private JPAQuery<Booking> getNextBookingSelectQuery(BooleanExpression whereExp) {
        var orderBy = booking.start.asc();
        return getBookingSelectQuery(whereExp, orderBy);
    }
}

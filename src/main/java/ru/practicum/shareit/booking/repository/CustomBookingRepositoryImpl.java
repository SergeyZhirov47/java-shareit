package ru.practicum.shareit.booking.repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilderFactory;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.Querydsl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStateForSearch;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.QBooking;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

public class CustomBookingRepositoryImpl implements CustomBookingRepository {
    private final JPAQueryFactory queryFactory;
    private final QBooking booking = new QBooking("booking");
    private final QBooking subBooking = new QBooking("subBooking");
    @PersistenceContext
    private EntityManager entityManager;

    public CustomBookingRepositoryImpl(EntityManager entityManager) {
        queryFactory = new JPAQueryFactory(JPQLTemplates.DEFAULT, entityManager);
    }

    // Условие - Все заявки на бронирование вещей данного пользователя.
    private static BooleanExpression getBookingsByItemsOwnerExpression(long ownerId) {
        return QBooking.booking.item.owner.id.eq(ownerId);
    }

    // Условие -Все заявки на бронирование, созданные пользователем.
    private static BooleanExpression getBookingsCreateByUserExpression(long userId) {
        return QBooking.booking.booker.id.eq(userId);
    }

    @Override
    public Booking getLastBookingForItemById(long itemId, LocalDateTime endDate) {
        final QBookingQueryHelper helper = new QBookingQueryHelper(booking);

        final BooleanExpression whereExp = helper.getLastBooking(itemId, endDate);
        final JPAQuery<Booking> query = helper.getLastBookingSelectQuery(whereExp);

        return query.fetchFirst();
    }

    @Override
    public Map<Long, Booking> getLastBookingForItemsByIdList(List<Long> itemIdList, LocalDateTime endDate) {
        final QBookingQueryHelper helper = new QBookingQueryHelper(booking);
        final QBookingQueryHelper subHelper = new QBookingQueryHelper(subBooking);

        var subQuery = JPAExpressions.select(subBooking.start.max())
                .from(subBooking)
                .where(subHelper.getLastBooking(itemIdList, endDate)
                        .and(subBooking.item.id.eq(booking.item.id)))
                .groupBy(subBooking.item.id);

        final BooleanExpression whereExp = helper.getLastBooking(itemIdList, endDate).and(booking.start.eq(subQuery));
        final List<Booking> lastBookings = queryFactory.selectFrom(booking).where(whereExp).fetch();

        return lastBookings.stream().collect(Collectors.toUnmodifiableMap(b -> b.getItem().getId(), b -> b));
    }

    @Override
    public Booking getNextBookingForItemById(long itemId, LocalDateTime startDate) {
        final QBookingQueryHelper helper = new QBookingQueryHelper(booking);

        final BooleanExpression whereExp = helper.getNextBooking(itemId, startDate);
        final JPAQuery<Booking> query = helper.getNextBookingSelectQuery(whereExp);

        return query.fetchFirst();
    }

    @Override
    public Map<Long, Booking> getNextBookingForItemsByIdList(List<Long> itemIdList, LocalDateTime startDate) {
        final QBookingQueryHelper helper = new QBookingQueryHelper(booking);
        final QBookingQueryHelper subHelper = new QBookingQueryHelper(subBooking);

        var subQuery = JPAExpressions.select(subBooking.start.min())
                .from(subBooking)
                .where(subHelper.getNextBooking(itemIdList, startDate)
                        .and(subBooking.item.id.eq(booking.item.id)))
                .groupBy(subBooking.item.id);

        final BooleanExpression whereExp = helper.getNextBooking(itemIdList, startDate).and(booking.start.eq(subQuery));
        final List<Booking> nextBookings = queryFactory.selectFrom(booking).where(whereExp).fetch();

        return nextBookings.stream().collect(Collectors.toUnmodifiableMap(b -> b.getItem().getId(), b -> b));
    }

    @Override
    public boolean isUserBookingItem(long userId, long itemId, LocalDateTime startUsingBeforeDate) {
        final QBookingQueryHelper helper = new QBookingQueryHelper(booking);

        final BooleanExpression isUserBookerExp = booking.booker.id.eq(userId);
        final BooleanExpression bookingPeriodExp = booking.start.before(startUsingBeforeDate); // неважно закончилась аренда или нет. главное, что уже начал пользоваться.
        final BooleanExpression whereExp = isUserBookerExp
                .and(helper.getByItemId(itemId))
                .and(helper.getApproved())
                .and(bookingPeriodExp);

        final Booking oneOfBooking = queryFactory.selectFrom(booking)
                .where(whereExp)
                .fetchFirst();

        return nonNull(oneOfBooking);
    }

    @Override
    public List<Booking> getUserBookingsByState(long userId, BookingStateForSearch searchState) {
        final BooleanExpression userBookingsExpression = getBookingsCreateByUserExpression(userId);
        return getBookingsBySearchState(userBookingsExpression, searchState, null);

    }

    @Override
    public List<Booking> getBookingsByItemOwner(long ownerId, BookingStateForSearch searchState) {
        // Все заявки на бронирование вещей данного пользователя.
        final BooleanExpression bookingsByItemsOwnerExpression = getBookingsByItemsOwnerExpression(ownerId);
        return getBookingsBySearchState(bookingsByItemsOwnerExpression, searchState, null);
    }

    @Override
    public List<Booking> getUserBookingsByState(long userId, BookingStateForSearch searchState, Pageable pageable) {
        final BooleanExpression userBookingsExpression = getBookingsCreateByUserExpression(userId);
        return getBookingsBySearchState(userBookingsExpression, searchState, pageable);
    }

    @Override
    public List<Booking> getBookingsByItemOwner(long ownerId, BookingStateForSearch searchState, Pageable pageable) {
        final BooleanExpression bookingsByItemsOwnerExpression = getBookingsByItemsOwnerExpression(ownerId);
        return getBookingsBySearchState(bookingsByItemsOwnerExpression, searchState, pageable);
    }

    private List<Booking> getBookingsBySearchState(BooleanExpression expression, BookingStateForSearch searchState, Pageable pageable) {
        final BooleanExpression searchStateExpression = getSearchExpressionByState(searchState); // условие сформированное исходя из searchState.
        final BooleanExpression finalExpression = expression.and(searchStateExpression);

        final JPAQuery<Booking> query = queryFactory.selectFrom(QBooking.booking)
                .where(finalExpression)
                .orderBy(QBooking.booking.start.desc());

        final List<Booking> result;
        if (nonNull(pageable)) {
            final Querydsl querydsl = new Querydsl(entityManager, (new PathBuilderFactory()).create(QBooking.class));
            result = querydsl.applyPagination(pageable, query).fetch();
        } else {
            result = query.fetch();
        }

        return result;
    }

    private BooleanExpression getSearchExpressionByState(BookingStateForSearch searchState) {
        BooleanExpression searchStateExpression = null;
        final LocalDateTime now = LocalDateTime.now();

        switch (searchState) {
            case PAST: {
                searchStateExpression = QBooking.booking.end.before(now);
                break;
            }
            case FUTURE: {
                searchStateExpression = QBooking.booking.start.after(now);
                break;
            }
            case CURRENT: {
                searchStateExpression = QBooking.booking.start.before(now).and(QBooking.booking.end.after(now));
                break;
            }
            case WAITING: {
                searchStateExpression = QBooking.booking.status.eq(BookingStatus.WAITING);
                break;
            }
            case REJECTED: {
                searchStateExpression = QBooking.booking.status.eq(BookingStatus.REJECTED);
                break;
            }
        }

        return searchStateExpression;
    }

    @RequiredArgsConstructor
    class QBookingQueryHelper {
        private final QBooking booking;

        private BooleanExpression getByItemId(long itemId) {
            return booking.item.id.eq(itemId);
        }

        private BooleanExpression getByItemIdList(List<Long> itemIds) {
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

        private BooleanExpression getLastBooking(long itemId, LocalDateTime endDate) {
            return getByItemId(itemId).and(getApproved()).and(getLastBooking(endDate));
        }

        private BooleanExpression getLastBooking(List<Long> itemIds, LocalDateTime endDate) {
            return getByItemIdList(itemIds).and(getApproved()).and(getLastBooking(endDate));
        }

        private BooleanExpression getNextBooking(long itemId, LocalDateTime startDate) {
            return getByItemId(itemId).and(getApproved()).and(getNextBooking(startDate));
        }

        private BooleanExpression getNextBooking(List<Long> itemIds, LocalDateTime startDate) {
            return getByItemIdList(itemIds).and(getApproved()).and(getNextBooking(startDate));
        }

        private JPAQuery<Booking> getBookingSelectQuery(BooleanExpression whereExp, OrderSpecifier<LocalDateTime> orderByDate) {
            return queryFactory.selectFrom(booking)
                    .where(whereExp)
                    .orderBy(orderByDate);
        }

        private JPAQuery<Booking> getLastBookingSelectQuery(BooleanExpression whereExp) {
            var orderBy = booking.start.desc();
            return getBookingSelectQuery(whereExp, orderBy);
        }

        private JPAQuery<Booking> getNextBookingSelectQuery(BooleanExpression whereExp) {
            var orderBy = booking.start.asc();
            return getBookingSelectQuery(whereExp, orderBy);
        }
    }
}

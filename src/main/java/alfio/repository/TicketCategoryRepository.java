/**
 * This file is part of alf.io.
 *
 * alf.io is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * alf.io is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with alf.io.  If not, see <http://www.gnu.org/licenses/>.
 */
package alfio.repository;

import alfio.model.TicketCategory;
import alfio.model.TicketCategoryStatisticView;
import ch.digitalfondue.npjt.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@QueryRepository
public interface TicketCategoryRepository {

    @Query("insert into ticket_category(inception, expiration, name, max_tickets, price_cts, src_price_cts, access_restricted, tc_status, event_id, bounded) " +
            "values(:inception, :expiration, :name, :max_tickets, 0, :price, :accessRestricted, 'ACTIVE', :eventId, :bounded)")
    @AutoGeneratedKey("id")
    AffectedRowCountAndKey<Integer> insert(@Bind("inception") ZonedDateTime inception,
                                  @Bind("expiration") ZonedDateTime expiration,
                                  @Bind("name") String name,
                                  @Bind("max_tickets") int maxTickets,
                                  @Bind("accessRestricted") boolean accessRestricted,
                                  @Bind("eventId") int eventId,
                                  @Bind("bounded") boolean bounded,
                                  @Bind("price") int price);

    @Query("select * from ticket_category where id = :id and event_id = :eventId and tc_status = 'ACTIVE'")
    TicketCategory getById(@Bind("id") int id, @Bind("eventId") int eventId);

    @Query("select * from ticket_category where id = :id and tc_status = 'ACTIVE'")
    Optional<TicketCategory> getById(@Bind("id") int id);

    @Query("select count(*) from ticket_category where event_id = :eventId and tc_status = 'ACTIVE' and bounded = false")
    Integer countUnboundedCategoriesByEventId(@Bind("eventId") int eventId);

    @Query("select * from ticket_category where event_id = :eventId and tc_status = 'ACTIVE' and bounded = false order by expiration desc")
    List<TicketCategory> findUnboundedOrderByExpirationDesc(@Bind("eventId") int eventId);

    @Query("select * from ticket_category where event_id = :eventId  and tc_status = 'ACTIVE' order by inception asc, expiration asc, id asc")
    List<TicketCategory> findAllTicketCategories(@Bind("eventId") int eventId);
    
    @Query("select * from ticket_category where event_id = :eventId")
    List<TicketCategory> findByEventId(@Bind("eventId") int eventId);

    @Query("select id from ticket_category where event_id = :eventId")
    List<Integer> findIdsByEventId(@Bind("eventId") int eventId);
    
    @Query("select count(*) from ticket_category where event_id = :eventId and access_restricted = true")
    Integer countAccessRestrictedRepositoryByEventId(@Bind("eventId") int eventId);

    @Query("update ticket_category set name = :name, inception = :inception, expiration = :expiration, " +
            "max_tickets = :max_tickets, src_price_cts = :price, access_restricted = :accessRestricted where id = :id")
    int update(@Bind("id") int id,
               @Bind("name") String name,
               @Bind("inception") ZonedDateTime inception,
               @Bind("expiration") ZonedDateTime expiration,
               @Bind("max_tickets") int maxTickets,
               @Bind("accessRestricted") boolean accessRestricted,
               @Bind("price") int price);

    @Query("update ticket_category set max_tickets = :max_tickets where id = :id")
    int updateSeatsAvailability(@Bind("id") int id, @Bind("max_tickets") int maxTickets);

    @Query("update ticket_category set bounded = :bounded where id = :id")
    int updateBoundedFlag(@Bind("id") int id, @Bind("bounded") boolean bounded);

    @Query("update ticket_category set inception = :inception, expiration = :expiration where id = :id")
    int fixDates(@Bind("id") int id, @Bind("inception") ZonedDateTime inception, @Bind("expiration") ZonedDateTime expiration);

    default int getTicketAllocation(int eventId) {
        return findByEventId(eventId).stream()
            .filter(TicketCategory::isBounded)
            .mapToInt(TicketCategory::getMaxTickets)
            .sum();
    }

    @Query("select * from TICKET_CATEGORY_STATISTICS where event_id = :eventId")
    List<TicketCategoryStatisticView> findStatisticsForEventId(@Bind("eventId") int eventId);

    default Map<Integer, TicketCategoryStatisticView> findStatisticsForEventIdByCategoryId(int eventId) {
        return findStatisticsForEventId(eventId).stream().collect(Collectors.toMap(TicketCategoryStatisticView::getId, Function.identity()));
    }
}

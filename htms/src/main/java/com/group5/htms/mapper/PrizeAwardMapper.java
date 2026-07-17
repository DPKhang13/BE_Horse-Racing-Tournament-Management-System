package com.group5.htms.mapper;

import com.group5.htms.dto.prize.response.PrizeAwardResponse;
import com.group5.htms.entity.HorseOwnerProfiles;
import com.group5.htms.entity.Horses;
import com.group5.htms.entity.PrizeAwards;
import com.group5.htms.entity.PrizeDistributions;
import com.group5.htms.entity.RaceResults;
import com.group5.htms.entity.Races;
import com.group5.htms.entity.Tournaments;
import com.group5.htms.entity.Users;
import org.springframework.stereotype.Component;

@Component
public class PrizeAwardMapper {

    public PrizeAwardResponse toResponse(PrizeAwards award) {
        if (award == null) {
            return null;
        }

        PrizeDistributions prize = award.getPrize();
        Tournaments tournament = award.getTournaments();
        Races race = award.getRace();
        RaceResults result = award.getResult();
        Horses horse = award.getHorse();
        HorseOwnerProfiles owner = award.getOwner();
        Users ownerUser = owner != null ? owner.getUsers() : null;

        return PrizeAwardResponse.builder()
                .awardId(award.getId())
                .prizeId(prize != null ? prize.getId() : null)
                .tournamentId(tournament != null ? tournament.getId() : null)
                .raceId(race != null ? race.getId() : null)
                .resultId(result != null ? result.getId() : null)
                .horseId(horse != null ? horse.getId() : null)
                .ownerId(owner != null ? owner.getId() : null)
                .finishPosition(award.getFinishPosition())
                .amount(award.getAmount())
                .status(award.getStatus())
                .awardedAt(award.getAwardedAt())
                .horseName(horse != null ? horse.getName() : null)
                .ownerFullName(ownerUser != null ? ownerUser.getFullName() : null)
                .tournamentName(tournament != null ? tournament.getName() : null)
                .prizeName(prize != null ? prize.getPrizeName() : null)
                .build();
    }
}

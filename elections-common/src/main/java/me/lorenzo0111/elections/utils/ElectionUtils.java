package me.lorenzo0111.elections.utils;

import me.lorenzo0111.elections.api.objects.Vote;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ElectionUtils {

    public static @NotNull List<String> getWinners(List<Vote> votes) {
        Map<String, Integer> counts = new HashMap<>();
        List<String> winners = new ArrayList<>();
        int winnerVotes = -1;

        for (Vote vote : votes) {
            if (!counts.containsKey(vote.getParty())) {
                counts.put(vote.getParty(), 1);
                continue;
            }

            Integer count = counts.get(vote.getParty());
            counts.replace(vote.getParty(), count, count + 1);
        }

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (winnerVotes < entry.getValue()) {
                winners.clear();
                winners.add(entry.getKey());
                winnerVotes = entry.getValue();
            } else if (winnerVotes == entry.getValue()) {
                winners.add(entry.getKey());
            }
        }
        return winners;
    }


}

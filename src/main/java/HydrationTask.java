import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HydrationTask implements Runnable{
    private final MessageChannel channel;
    private final String author;
    private final ScheduledExecutorService executor;
    private final ConcurrentHashMap<String, Integer> peopleTime;

    public HydrationTask(MessageChannel channel, String author, ScheduledExecutorService executor, ConcurrentHashMap<String, Integer> peopleTime) {
        this.channel = channel;
        this.author = author;
        this.executor = executor;
        this.peopleTime = peopleTime;
    }


    @Override
    public void run() {
        if (peopleTime.containsKey(author)) {
            channel.sendMessage(author + " Drink some water").queue();
            int time = peopleTime.get(author);
            HydrationTask runnable = new HydrationTask(channel, author, executor, peopleTime);
            executor.schedule(runnable, time, TimeUnit.MINUTES);
        }
    }
}

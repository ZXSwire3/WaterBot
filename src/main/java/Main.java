import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {

    public static char call = '~';
    private final ConcurrentHashMap<String, Integer> peopleTime = new ConcurrentHashMap<>();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws LoginException {
        InputStream file = Main.class.getResourceAsStream("token.txt");
        String token;
        Scanner fileReader = new Scanner(file);
        if (!fileReader.hasNextLine()) {
            System.err.println("No token entered. " +
                    "\nPlease enter the bot's token file in token.txt file");
        } else {
            token = fileReader.nextLine();
            JDABuilder builder = JDABuilder.createDefault(token);
            builder.addEventListeners(new Main());
            builder.build();
            System.out.println("Ready to HYDRATE!");
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();
        String author = event.getAuthor().getAsMention();
        Message msg = event.getMessage();

        System.out.println("We received a message from " +
                event.getAuthor().getName() + ": " +
                event.getMessage().getContentDisplay());

        if (msg.getContentRaw().contains(call + "")) {
            if (msg.getContentRaw().equalsIgnoreCase(call + "hydrate")|| msg.getContentRaw().equalsIgnoreCase(call + "h")) {
                hydrate(channel, author, msg);
            } else if (msg.getContentRaw().equalsIgnoreCase(call + "hTime")) {
                channel.sendMessage("invalid choice, must be input as \"~htime time\"\n" +
                        "for example \"~htime 30\"\n" +
                        "this will remind you every 30 minutes").queue();
            } else if (msg.getContentRaw().toLowerCase().contains(call + "htime ")) {
                String[] input = msg.getContentDisplay().split(" ");
                System.out.println(input[0]);
                System.out.println(input[1]);
                int time = Integer.parseInt(input[1]);
                System.out.println(time);
                //String units = input[2];
                hydrateTime(channel, author, msg, time);

            } else if (msg.getContentRaw().equalsIgnoreCase(call + "stop")) {
                peopleTime.remove(author);
                channel.sendMessage(author + "will no longer be reminded to drink water").queue();
            } else if (msg.getContentRaw().equalsIgnoreCase(call + "help")) {
                channel.sendMessage(">>> Welcome to **__hydration bot!!!__**\n" +
                        "The following are all valid commands: \n" +
                        "**~hydrate** or **~h**: Reminds you to drink some water every 30 min\n" +
                        "**~htime (time)**: Reminds you to drink some water at a given interval in minutes\n" +
                        "**~help**: HEY! that's this command :D").queue();
            } else if (msg.getContentRaw().startsWith(String.valueOf(call))) {
                channel.sendMessage(">>> Command unrecognized\n" +
                                         "Type **__~help**__ for commands").queue();

            }
        }

    }

    public void hydrate(MessageChannel channel, String author, Message msg) {
        hydrateTime(channel, author, msg, 30);
    }

    public void hydrateTime(MessageChannel channel, String author, Message msg, int time) {
        if (peopleTime.putIfAbsent(author, time) == null) {
            HydrationTask test = new HydrationTask(channel, author, executor, peopleTime);

            if (time == 1) {
                channel.sendMessage("Reminding " + author + " to stay hydrated every minute").queue();
                executor.schedule(test, time, TimeUnit.MINUTES);
            } else if (time > 1) {
                channel.sendMessage("Reminding " + author + " to stay hydrated every " + time + " minutes").queue();
                executor.schedule(test, time, TimeUnit.MINUTES);
            } else {
                channel.sendMessage("Invalid time, time must be 1 or greater").queue();
            }

        } else {

            if (time == 1) {
                channel.sendMessage("Rescheduling to remind " + author + " to stay hydrated every minute").queue();
                peopleTime.replace(author, time);
            } else if (time > 1) {
                channel.sendMessage("Rescheduling to remind " + author + " to stay hydrated every " + time + " minutes").queue();
                peopleTime.replace(author, time);
            } else {
                channel.sendMessage("Invalid time, time must be 1 or greater").queue();
            }
        }

    }
}

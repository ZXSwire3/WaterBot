import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends ListenerAdapter {

    //public static ArrayList<String> dehydratedPeople = new ArrayList<>();\
    public static String[] dehydratedPeople = new String[10];
    public static char call = '~';
    //public static ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) throws LoginException {
        String token = "insert token here";
        JDABuilder builder = JDABuilder.createDefault(token);
        builder.addEventListeners(new Main());
        builder.build();
        System.out.println("Ready to HYDRATE!");
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
            if (msg.getContentRaw().equalsIgnoreCase(call + "hydrate")) {
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

            } else if (msg.getContentRaw().contains("stop")) {
                stop();
            }
        }

    }

    public void hydrate(MessageChannel channel, String author, Message msg) {
        hydrateTime(channel, author, msg, 30);
    }

    public void hydrateTime(MessageChannel channel, String author, Message msg, int time) {
//        for (int i = 0; i < dehydratedPeople.length; i++) {
//            if (author.equalsIgnoreCase(dehydratedPeople[i])) {
//                channel.sendMessage("Already reminding " + author).queue();
//            } else if (dehydratedPeople[i] == null){
//                dehydratedPeople[i] = author;
//
//            }
//        }
        boolean exists = false;
        int temp = 0;
        for (int i = 0; i < dehydratedPeople.length; i++) {
            exists = author.equalsIgnoreCase(dehydratedPeople[i]);
            if (exists) {
                temp = i;
            } else {
                dehydratedPeople[i] = author;
            }
        }
        System.out.println(dehydratedPeople[temp] + exists);

        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        if (exists) {
            ses.shutdownNow();
            ses = Executors.newSingleThreadScheduledExecutor();
        }

        if (time == 1) {
            channel.sendMessage("Reminding " + author + " to stay hydrated every minute").queue();
        } else if (time > 1) {
            channel.sendMessage("Reminding " + author + " to stay hydrated every " + time + " minutes").queue();
        } else {
            channel.sendMessage("Invalid time, time must be 1 or greater").queue();
        }


        ses.scheduleAtFixedRate(() -> channel.sendMessage(author + " Drink some water").queue(), time, time, TimeUnit.MINUTES);
    }

    public void stop() {
       // ses.shutdown();
    }
}

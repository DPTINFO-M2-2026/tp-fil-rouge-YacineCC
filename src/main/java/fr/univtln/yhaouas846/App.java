package fr.univtln.yhaouas846;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;


public class App {
    Mono<Void> login = client.withGateway((GatewayDiscordClient gateway) -> {
  // ReadyEvent example
  Mono<Void> printOnLogin = gateway.on(ReadyEvent.class, event ->
      Mono.fromRunnable(() -> {
        final User self = event.getSelf();
        System.out.printf("Logged in as %s#%s%n", self.getUsername(), self.getDiscriminator());
      }))
      .then();

  // MessageCreateEvent example
  Mono<Void> handlePingCommand = gateway.on(MessageCreateEvent.class, event -> {
    Message message = event.getMessage();

    if (message.getContent().equalsIgnoreCase("!ping")) {
      return message.getChannel()
          .flatMap(channel -> channel.createMessage("pong!"));
    }

    return Mono.empty();
  }).then();

  // combine them!
  return printOnLogin.and(handlePingCommand);
});
    
}
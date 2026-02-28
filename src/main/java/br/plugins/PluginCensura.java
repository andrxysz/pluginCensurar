package br.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class PluginCensura extends JavaPlugin implements Listener {

    private static final String BYPASS_PERMISSION = "plugincensura.bypass";
    private List<String> palavroes = new ArrayList<String>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadpalavroes();
        getServer().getPluginManager().registerEvents(this, this);
    }

    private void loadpalavroes() {
        List<String> configuredWords = getConfig().getStringList("palavroes");
        palavroes.clear();

        for (String word : configuredWords) {
            if (word != null && !word.trim().isEmpty()) {
                palavroes.add(word.trim());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (isPlayerImmune(event.getPlayer())) {
            return;
        }

        String originalMessage = event.getMessage();
        String censoredMessage = censurar(originalMessage);

        if (!originalMessage.equals(censoredMessage)) {
            event.setMessage(censoredMessage);
        }
    }

    private boolean isPlayerImmune(Player player) {
        boolean adminImmune = getConfig().getBoolean("admin-immune", true);
        if (!adminImmune) {
            return false;
        }

        return player.isOp() || player.hasPermission(BYPASS_PERMISSION);
    }

    private String censurar(String message) {
        String result = message;

        for (String badWord : palavroes) {
            result = replaceIgnoringCase(result, badWord);
        }

        return result;
    }

    private String replaceIgnoringCase(String text, String target) {
        Pattern pattern = Pattern.compile(Pattern.quote(target), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        Matcher matcher = pattern.matcher(text);
        StringBuffer output = new StringBuffer();

        while (matcher.find()) {
            matcher.appendReplacement(output, Matcher.quoteReplacement(mascarar(matcher.group().length())));
        }

        matcher.appendTail(output);
        return output.toString();
    }

    private String mascarar(int length) {
        char[] mascararChars = new char[length];
        Arrays.fill(mascararChars, '*');
        return new String(mascararChars);
    }
}

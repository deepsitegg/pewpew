package gg.deepsite.pewpew.utils;

import lombok.experimental.PackagePrivate;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public final class ChatUtils {
    public static TextColor PRIMARY = TextColor.fromHexString("#ff5151");
    public static TextColor SUCCESS = TextColor.fromHexString("#12ff2a");
    public static TextColor ERROR = TextColor.fromHexString("#FC3838");
    public static TextColor WARNING = TextColor.fromHexString("#FBFB00");

    public static Component prefix(String message, TextColor textColor, Object... args) {
        return format(getPrefix(textColor) + message, textColor, args);
    }

    public static Component prefix(String message, Object... args) {
        return format(getPrefix(PRIMARY) + message, args);
    }

    @PackagePrivate
    private static String getPrefix(TextColor textColor) {
        return "<" + tint(textColor.asHexString(), 0.15) + ">•<" + textColor.asHexString() + ">● " + "Pewpew" + " <dark_gray>┃ <gray>";
    }

    public static Component format(String message, Object... args) {
        return format(message, PRIMARY, args);
    }

    public static Component format(String message, TextColor textColor, Object... args) {
        MiniMessage extendedInstance = MiniMessage.builder()
                .preProcessor(string -> "<!i>" + string)
                .editTags(tags -> {
                    tags.resolver(TagResolver.resolver("primary", Tag.styling(PRIMARY)));
                    tags.resolver(TagResolver.resolver("success", Tag.styling(SUCCESS)));
                    tags.resolver(TagResolver.resolver("error", Tag.styling(ERROR)));
                    tags.resolver(TagResolver.resolver("warning", Tag.styling(WARNING)));

                    tags.resolver(TagResolver.resolver("color", Tag.styling(textColor)));
                    tags.resolver(TagResolver.resolver("color_alt", Tag.styling(TextColor.fromHexString(tint(textColor.asHexString(), 0.15)))));
                }).build();

        return extendedInstance.deserialize(replaceArguments(message, args));
    }

    @PackagePrivate
    private static String replaceArguments(String message, Object... replacements) {
        try {
            for (int i = 0; i < replacements.length; i++) {
                String placeholder = "%" + (i + 1);
                message = message.replaceAll(placeholder + "(?![0-9])", String.valueOf(replacements[i]));
            }
            return message;
        } catch (Exception ignored) {
            return message;
        }
    }

    public static Component formatLinks(String message, String chatColor) {
        ComponentBuilder<TextComponent, TextComponent.Builder> messageComponent = Component.empty().toBuilder();
        Pattern pattern = Pattern.compile("(http|https)://[\\w\\-.]+(:\\d+)?(/\\S*)?");
        Matcher matcher = pattern.matcher(message);

        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String beforeUrl = message.substring(lastEnd, matcher.start());
                messageComponent.append(Component.text(beforeUrl).style(ChatUtils.format(chatColor).style()));
            }

            String url = matcher.group();
            messageComponent.append(Component.text(url).style(ChatUtils.format(chatColor).style()).clickEvent(ClickEvent.openUrl(url)));
            lastEnd = matcher.end();
        }

        if (lastEnd < message.length()) {
            String remainingText = message.substring(lastEnd);
            messageComponent.append(Component.text(remainingText).style(ChatUtils.format(chatColor).style()));
        }

        return messageComponent.build();
    }

    @PackagePrivate
    public static String tint(String hexColor, double factor) {
        int red = Integer.parseInt(hexColor.substring(1, 3), 16);
        int green = Integer.parseInt(hexColor.substring(3, 5), 16);
        int blue = Integer.parseInt(hexColor.substring(5, 7), 16);

        red = (int) Math.round(Math.min(255, red + (255 - red) * factor));
        green = (int) Math.round(Math.min(255, green + (255 - green) * factor));
        blue = (int) Math.round(Math.min(255, blue + (255 - blue) * factor));

        return String.format("#%02x%02x%02x", red, green, blue);
    }

}

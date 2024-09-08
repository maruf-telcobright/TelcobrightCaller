package com.example.telcobrightcaller.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SDPParser {

    public static String filterCodecs(String sdp) {
        Pattern codecPattern = Pattern.compile("(a=rtpmap:\\d+ (?!opus|telephone-event).+?\\n)");
        Matcher matcher = codecPattern.matcher(sdp);
        StringBuffer filteredSDP = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(filteredSDP, "");
        }
        matcher.appendTail(filteredSDP);
        String input = filteredSDP.toString();
        List<String> result = getCodecCodes(input);
        String mLine = updateCodecs(input,result);
        return replaceAudioLine(input, mLine);
    }

    public static List<String> getCodecCodes(String input) {
        List<String> codecCodes = new ArrayList<>();
        String[] lines = input.split("\n");

        for (String line : lines) {
            if (line.startsWith("a=")) {
                String[] parts = line.split(" ");
                for (int i = 0; i < parts.length; i++) {
                    if (parts[i].startsWith("a=rtpmap:")) {
                        codecCodes.add(parts[i].split(":")[1]);
                    }
                }
            }
        }

        return codecCodes;
    }
    public static String updateCodecs(String inputString, List<String> supportedCodecs) {
        String[] lines = inputString.split("\n");
        String mLine = "";
        for (String line : lines) {
            if (line.startsWith("m=audio")) {
                String[] parts = line.split(" ");
                List<String> updatedCodecs = new ArrayList<>();
                updatedCodecs.add(parts[0]); // Add "m=audio"
                updatedCodecs.add(parts[1]); // Add "9"
                updatedCodecs.add(parts[2]); // Add "UDP/TLS/RTP/SAVPF"

                for (int i = 3; i < parts.length; i++) {
                    if (supportedCodecs.contains(parts[i])) {
                        updatedCodecs.add(parts[i]);
                    }
                }
                mLine = String.join(" ",updatedCodecs);
            }
        }
        return mLine;
    }

    public static String replaceAudioLine(String inputString, String mLine) {
        String[] lines = inputString.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("m=audio")) {
                result.append(mLine).append("\n");
            } else {
                result.append(line).append("\n");
            }
        }

        return result.toString();
    }
}


package com.simplecity.amp_library.playback.constants;

public enum MediaButtonCommand {
    CMD_NAME("command"),
    TOGGLE_PAUSE("togglepause"),
    STOP("stop"),
    PAUSE("pause"),
    PLAY("play"),
    PREVIOUS("previous"),
    NEXT("next"),
    TOGGLE_FAVORITE("togglefavorite"),
    FROM_MEDIA_BUTTON("frommediabutton"),
    FORCE_PREVIOUS("force");

    private final String command;

    MediaButtonCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

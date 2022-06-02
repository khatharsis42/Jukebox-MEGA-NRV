import mpv
import requests
import sys
import signal
import itertools
import multiprocessing

from typing import (
    Iterable,
    List,
    NamedTuple
)


def get_player() -> mpv.MPV:
    """Returns a new instance of MPV player, in case none exists or old one was broken."""
    return mpv.MPV(ytdl=True)


# MPV player
player: mpv.MPV = get_player()


class SyncData(NamedTuple):
    """Representation of data received from server."""
    playlist: List[str]
    ids: List[int]


def _next(sig_num, stack_frame):
    """On signal, asking script to skip current song."""
    global to_skip
    to_skip = True


def _update(sig_num, stack_frame):
    """On signal, asking script to ask for a playlist update."""
    global to_update
    to_update = True


def update():
    """Requests server the new state of playlist, and updates player appropriately."""
    updated_playlist = SyncData(**requests.get(url).json()).playlist

    player.playlist_clear()
    for url_or_path in updated_playlist:
        player.playlist_append(url_or_path)


def wait():
    global done
    player.wait_for_playback()
    done = True


if __name__ == "__main__":
    # Checking if request url was provided
    if not (args := sys.argv[1:]):
        raise ValueError("Host name required.")

    # Global variables for script
    done = False
    to_skip = False
    to_update = True
    url = args[0]

    # Registering end and update signals
    signal.signal(signal.SIGUSR1, _next)           # Custom signal
    signal.signal(signal.SIGUSR2, _update)         # Custom signal

    while True:
        # DEBUG: printing playlist
        print(player.playlist_filenames)

        try:
            # Waiting for track to end or skip signal
            waiting = multiprocessing.Process(target=wait)
            waiting.start()
            while not to_skip or not done:
                pass

            waiting.terminate()
            done = False
            # If skip signal received, setting to_skip value back to False
            if to_skip:
                to_skip = False

            # Removing the track that just ended playing
            player.playlist_remove(0)

        # An error that happens sometimes, just
        # ignoring it and restarting player
        except mpv.ShutdownError as e:
            player: mpv.MPV = get_player()

        # If update signal was received, resets playlist
        # by requesting new playlist state
        if to_update:
            update()
            to_update = False

        # Sending back the current state of player to server
        requests.post(url, ids)

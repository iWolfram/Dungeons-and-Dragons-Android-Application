package ca.nait.rresuena1.dungeonsdragonsapplication;

import android.media.AudioManager;
import android.media.SoundPool;

/**
 * Created by Rameses on 4/12/2018.
 */

final class PreLollipopSoundPool {
    @SuppressWarnings("deprecation")
    public static SoundPool NewSoundPool() {
        return new SoundPool(1, AudioManager.STREAM_MUSIC,0);
    }
}

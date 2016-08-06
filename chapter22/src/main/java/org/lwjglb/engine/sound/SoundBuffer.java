package org.lwjglb.engine.sound;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.openal.AL10.*;
import org.lwjgl.stb.STBVorbisInfo;
import java.nio.ShortBuffer;
import org.lwjgl.BufferUtils;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryUtil.*;
import org.lwjglb.engine.Utils;

public class SoundBuffer {

    private final int buffer;

    public SoundBuffer(String file) throws Exception {
        this.buffer = alGenBuffers();
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer pcm = readVorbis(file, 32 * 1024, info);

            // Copy to buffer
            alBufferData(buffer, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
    }

    public int getBufferId() {
        return this.buffer;
    }
 
    public void cleanup() {
        alDeleteBuffers(this.buffer);
    }

    private ShortBuffer readVorbis(String resource, int bufferSize, STBVorbisInfo info) throws Exception {
        ByteBuffer vorbis;
        vorbis = Utils.ioResourceToByteBuffer(resource, bufferSize);

        IntBuffer error = BufferUtils.createIntBuffer(1);
        long decoder = stb_vorbis_open_memory(vorbis, error, null);
        if (decoder == NULL) {
            throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
        }

        stb_vorbis_get_info(decoder, info);

        int channels = info.channels();

        int lengthSamples = stb_vorbis_stream_length_in_samples(decoder);

        ShortBuffer pcm = BufferUtils.createShortBuffer(lengthSamples);

        pcm.limit(stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm) * channels);
        stb_vorbis_close(decoder);

        return pcm;
    }
}